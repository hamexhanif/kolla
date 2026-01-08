package team5.prototype.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import team5.prototype.dto.CreateUserRequestDto;
import team5.prototype.security.TenantProvider;
import team5.prototype.tenant.Tenant;
import team5.prototype.tenant.TenantRepository;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final TenantProvider tenantProvider;
    private final PasswordEncoder passwordEncoder;

    // Konstruktor-Injection: Spring liefert uns automatisch das UserRepository
    public UserServiceImpl(UserRepository userRepository,
                           TenantRepository tenantRepository,
                           TenantProvider tenantProvider,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.tenantProvider = tenantProvider;
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
                .build();
        return userRepository.save(newUser);
    }

    /**
     * Erstellt einen neuen Benutzer und speichert ihn in der Datenbank.
     * @param user Das zu erstellende User-Objekt.
     * @return Der gespeicherte User (inklusive der generierten ID).
     */
    public User createUser(User user) {
        // Später könnten hier Validierungen hinzugefügt werden (z.B. Passwortstärke prüfen)
        Long tenantId = tenantProvider.getCurrentTenantId();
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant mit ID " + tenantId + " nicht gefunden!"));
        user.setTenant(tenant);
        return userRepository.save(user);
    }

    // --- Die anderen Methoden bleiben größtenteils unverändert ---

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAllByTenant_Id(tenantProvider.getCurrentTenantId());
    }

    @Override
    public Optional<User> getUserById(Long userId) {
        return userRepository.findByIdAndTenant_Id(userId, tenantProvider.getCurrentTenantId());
    }

    @Override
    public User updateUser(Long userId, User userDetails) {
        // Finde den existierenden Benutzer oder wirf eine Ausnahme
        return userRepository.findByIdAndTenant_Id(userId, tenantProvider.getCurrentTenantId())
                .map(existingUser -> {
                    existingUser.setUsername(userDetails.getUsername());
                    existingUser.setEmail(userDetails.getEmail());
                    return userRepository.save(existingUser);
                })
                .orElseThrow(() -> new RuntimeException("Benutzer mit ID " + userId + " nicht gefunden!"));
    }

    @Override
    public void deleteUser(Long userId) {
        // Prüfen, ob der Benutzer existiert, bevor versucht wird, ihn zu löschen
        if (!userRepository.existsByIdAndTenant_Id(userId, tenantProvider.getCurrentTenantId())) {
            throw new RuntimeException("Benutzer mit ID " + userId + " nicht gefunden!");
        }
        userRepository.deleteById(userId);
    }
}
