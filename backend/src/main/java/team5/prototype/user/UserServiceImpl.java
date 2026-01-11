package team5.prototype.user;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import team5.prototype.dto.CreateUserRequestDto;
import team5.prototype.role.Role;
import team5.prototype.role.RoleRepository;
import team5.prototype.tenant.Tenant;
import team5.prototype.tenant.TenantRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    // Konstruktor wurde angepasst, um die neuen Abhängigkeiten zu erhalten
    public UserServiceImpl(UserRepository userRepository,
                           TenantRepository tenantRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ===================================================================
    // KORREKTUR: Die createUser-Methode wurde angepasst, um das DTO zu akzeptieren
    // und dem UserService-Interface zu entsprechen.
    // ===================================================================
    @Override
    public User createUser(CreateUserRequestDto requestDto) {
        // 1. Lade den Tenant aus der Datenbank
        Tenant tenant = tenantRepository.findById(requestDto.getTenantId())
                .orElseThrow(() -> new RuntimeException("Tenant mit ID " + requestDto.getTenantId() + " nicht gefunden"));

        // 2. Erstelle eine neue User-Entity aus dem DTO
        User newUser = User.builder()
                .username(requestDto.getUsername())
                .email(requestDto.getEmail())
                .passwordHash(passwordEncoder.encode(requestDto.getPassword())) // Passwort wird sicher gehasht
                .firstName(requestDto.getFirstName())
                .lastName(requestDto.getLastName())
                .tenant(tenant)
                .active(true)
                .roles(new HashSet<>())
                .build();

        // IMPORTANT: Fetch actual Role entity and add them to the user
        if (requestDto.getRoleId() != null) {
            Optional<Role> role = roleRepository.findById(requestDto.getRoleId());
            role.ifPresent(value -> newUser.getRoles().add(value));
        }

        // 3. Speichere den neuen User und gib ihn zurück
        return userRepository.save(newUser);
    }

    // --- Die anderen Methoden bleiben größtenteils unverändert ---

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAllActive();
    }

    @Override
    public Optional<User> getUserById(Long userId) {
        return userRepository.findByIdAndActive(userId);
    }

    @Override
    @Transactional
    public User updateUser(Long userId, UpdateUserRequestDto requestDto) {
        return userRepository.findById(userId)
                .map(existingUser -> {
                    // Update der Basis-Daten aus dem DTO
                    existingUser.setUsername(requestDto.getUsername());
                    existingUser.setEmail(requestDto.getEmail());

                    // KORREKTE LOGIK ZUM AKTUALISIEREN DER ROLLEN
                    if (requestDto.getRoleIds() != null) {
                        Set<Role> newRoles = new HashSet<>(roleRepository.findAllById(requestDto.getRoleIds()));
                        existingUser.getRoles().clear(); // Alte Rollen entfernen
                        existingUser.getRoles().addAll(newRoles); // Neue Rollen hinzufügen
                    }

                    // Die @Transactional-Annotation kümmert sich um das Speichern
                    return existingUser;
                })
                .orElseThrow(() -> new EntityNotFoundException("Benutzer mit ID " + userId + " nicht gefunden!"));
    }

    @Override
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Benutzer mit ID " + userId + " nicht gefunden!"));

        // Instead of deleting, mark as inactive (soft delete)
        user.setActive(false);
        userRepository.save(user);
    }
}