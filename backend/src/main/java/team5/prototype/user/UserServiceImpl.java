package team5.prototype.user;

import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           TenantRepository tenantRepository,
                           TenantProvider tenantProvider,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.tenantProvider = tenantProvider;
        this.passwordEncoder = passwordEncoder;
    }

    UserServiceImpl(UserRepository userRepository,
                    TenantRepository tenantRepository,
                    TenantProvider tenantProvider) {
        this(userRepository, tenantRepository, tenantProvider, null);
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
                .build();

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
    public User updateUser(Long userId, User userDetails) {
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
        Long tenantId = tenantProvider.getCurrentTenantId();
        if (!userRepository.existsByIdAndTenant_Id(userId, tenantId)) {
            throw new RuntimeException("Benutzer mit ID " + userId + " nicht gefunden!");
        }
        userRepository.deleteById(userId);
    }
}
