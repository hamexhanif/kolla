package team5.prototype.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team5.prototype.dto.CreateUserRequestDto;
import team5.prototype.role.Role;
import team5.prototype.role.RoleRepository;
import team5.prototype.security.TenantProvider;
import team5.prototype.tenant.Tenant;
import team5.prototype.tenant.TenantRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final TenantProvider tenantProvider;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           TenantRepository tenantRepository,
                           TenantProvider tenantProvider,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.tenantProvider = tenantProvider;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    UserServiceImpl(UserRepository userRepository,
                    TenantRepository tenantRepository,
                    TenantProvider tenantProvider) {
        this(userRepository, tenantRepository, tenantProvider, null, null);
    }

    @Override
    public User createUser(CreateUserRequestDto requestDto) {
        Long tenantId = Optional.ofNullable(requestDto.getTenantId())
                .orElseGet(tenantProvider::getCurrentTenantId);
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant mit ID " + tenantId + " nicht gefunden"));

        User newUser = User.builder()
                .username(requestDto.getUsername())
                .email(requestDto.getEmail())
                .passwordHash(passwordEncoder != null
                        ? passwordEncoder.encode(requestDto.getPassword())
                        : requestDto.getPassword())
                .firstName(requestDto.getFirstName())
                .lastName(requestDto.getLastName())
                .tenant(tenant)
                .active(true)
                .roles(new HashSet<>())
                .build();

        if (requestDto.getRoleId() != null && roleRepository != null) {
            Optional<Role> role = roleRepository.findById(requestDto.getRoleId());
            role.ifPresent(value -> newUser.getRoles().add(value));
        }

        return userRepository.save(newUser);
    }

    public User createUser(User user) {
        Long tenantId = tenantProvider.getCurrentTenantId();
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant mit ID " + tenantId + " nicht gefunden"));
        user.setTenant(tenant);
        return userRepository.save(user);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAllByTenant_Id(tenantProvider.getCurrentTenantId());
    }

    @Override
    public Optional<User> getUserById(Long userId) {
        return userRepository.findByIdAndTenant_Id(userId, tenantProvider.getCurrentTenantId());
    }

    @Override
    @Transactional
    public User updateUser(Long userId, UpdateUserRequestDto requestDto) {
        return userRepository.findByIdAndTenant_Id(userId, tenantProvider.getCurrentTenantId())
                .map(existingUser -> {
                    existingUser.setUsername(requestDto.getUsername());
                    existingUser.setEmail(requestDto.getEmail());

                    if (requestDto.getRoleIds() != null && roleRepository != null) {
                        Set<Role> newRoles = new HashSet<>(roleRepository.findAllById(requestDto.getRoleIds()));
                        existingUser.getRoles().clear();
                        existingUser.getRoles().addAll(newRoles);
                    }

                    return existingUser;
                })
                .orElseThrow(() -> new RuntimeException("Benutzer mit ID " + userId + " nicht gefunden!"));
    }

    @Override
    public void deleteUser(Long userId) {
        Long tenantId = tenantProvider.getCurrentTenantId();
        if (!userRepository.existsByIdAndTenant_Id(userId, tenantId)) {
            throw new RuntimeException("Benutzer mit ID " + userId + " nicht gefunden!");
        }
        userRepository.deleteById(userId);
    }
}
