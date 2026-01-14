package team5.prototype.user;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import team5.prototype.dto.CreateUserRequestDto;
import team5.prototype.role.Role;
import team5.prototype.role.RoleRepository;
import team5.prototype.tenant.Tenant;
import team5.prototype.tenant.TenantContext;
import team5.prototype.tenant.TenantRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           TenantRepository tenantRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User createUser(CreateUserRequestDto requestDto) {
        Long tenantId = currentTenantId();
        if (requestDto.getTenantId() != null && !tenantId.equals(requestDto.getTenantId())) {
            throw new EntityNotFoundException("Tenant mismatch");
        }
        // 1. Lade den Tenant aus der Datenbank
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Tenant mit ID " + tenantId + " nicht gefunden"));

        // Erstelle eine neue User-Entity aus dem DTO
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

        // Fetch actual Role entity and add them to the user
        if (requestDto.getRoleId() != null) {
            Optional<Role> role = roleRepository.findByIdAndTenantId(requestDto.getRoleId(), tenantId);
            role.ifPresent(value -> newUser.getRoles().add(value));
        }

        return userRepository.save(newUser);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAllActiveByTenantId(currentTenantId());
    }

    @Override
    public Optional<User> getUserById(Long userId) {
        return userRepository.findByIdAndTenantIdAndActive(userId, currentTenantId());
    }

    @Override
    @Transactional
    public User updateUser(Long userId, UpdateUserRequestDto requestDto) {
        Long tenantId = currentTenantId();
        return userRepository.findByIdAndTenantId(userId, tenantId)
                .map(existingUser -> {
                    existingUser.setUsername(requestDto.getUsername());
                    existingUser.setEmail(requestDto.getEmail());

                    if (requestDto.getRoleIds() != null) {
                        Set<Role> newRoles = roleRepository.findAllById(requestDto.getRoleIds()).stream()
                                .filter(role -> role.getTenant() != null && tenantId.equals(role.getTenant().getId()))
                                .collect(Collectors.toSet());
                        existingUser.getRoles().clear(); // Alte Rollen entfernen
                        existingUser.getRoles().addAll(newRoles); // Neue Rollen hinzufügen
                    }

                    return existingUser;
                })
                .orElseThrow(() -> new EntityNotFoundException("Benutzer mit ID " + userId + " nicht gefunden!"));
    }

    @Override
    public void deleteUser(Long userId) {
        User user = userRepository.findByIdAndTenantId(userId, currentTenantId())
                .orElseThrow(() -> new EntityNotFoundException("Benutzer mit ID " + userId + " nicht gefunden!"));

        // Instead of deleting, mark as inactive (soft delete)
        user.setActive(false);
        userRepository.save(user);
        // TODO: implement reassignment of tasks of the deleted user according to the rule used in Task Creation
        //  in TaskServiceImpl
    }

    private Long currentTenantId() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new EntityNotFoundException("Kein Tenant-Kontext vorhanden");
        }
        return tenantId;
    }
}
