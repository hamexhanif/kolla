package team5.prototype.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import team5.prototype.dto.CreateUserRequestDto;
import team5.prototype.tenant.Tenant;
import team5.prototype.tenant.TenantRepository;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;

    // Konstruktor wurde angepasst, um die neuen Abhängigkeiten zu erhalten
    public UserServiceImpl(UserRepository userRepository,
                           TenantRepository tenantRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
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

        // 3. Speichere den neuen User und gib ihn zurück
        return userRepository.save(newUser);
    }

    // --- Die anderen Methoden bleiben größtenteils unverändert ---

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }

    @Override
    public User updateUser(Long userId, User userDetails) {
        return userRepository.findById(userId)
                .map(existingUser -> {
                    existingUser.setUsername(userDetails.getUsername());
                    existingUser.setEmail(userDetails.getEmail());
                    return userRepository.save(existingUser);
                })
                .orElseThrow(() -> new RuntimeException("Benutzer mit ID " + userId + " nicht gefunden!"));
    }

    @Override
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("Benutzer mit ID " + userId + " nicht gefunden!");
        }
        userRepository.deleteById(userId);
    }
}