package team5.prototype.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import team5.prototype.dto.CreateUserRequestDto;
import team5.prototype.role.RoleRepository;
import team5.prototype.security.TenantProvider;
import team5.prototype.tenant.Tenant;
import team5.prototype.tenant.TenantRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private TenantProvider tenantProvider;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void createUserSavesEntity() {
        User user = User.builder().username("alex").email("alex@example.com").build();
        Tenant tenant = Tenant.builder().id(1L).name("t1").build();

        when(tenantProvider.getCurrentTenantId()).thenReturn(1L);
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant));
        when(userRepository.save(user)).thenReturn(user);

        User saved = userService.createUser(user);

        assertThat(saved).isEqualTo(user);
    }

    @Test
    void createUserUsesTenantProviderWhenDtoMissingTenantId() {
        CreateUserRequestDto request = new CreateUserRequestDto();
        request.setUsername("alex");
        request.setEmail("alex@example.com");
        request.setPassword("secret");

        Tenant tenant = Tenant.builder().id(2L).name("t2").build();

        when(tenantProvider.getCurrentTenantId()).thenReturn(2L);
        when(tenantRepository.findById(2L)).thenReturn(Optional.of(tenant));
        when(passwordEncoder.encode("secret")).thenReturn("secret");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User saved = userService.createUser(request);

        assertThat(saved.getTenant()).isEqualTo(tenant);
        assertThat(saved.getPasswordHash()).isEqualTo("secret");
    }

    @Test
    void createUserEncodesPasswordWhenEncoderAvailable() {
        CreateUserRequestDto request = new CreateUserRequestDto();
        request.setUsername("alex");
        request.setEmail("alex@example.com");
        request.setPassword("secret");

        Tenant tenant = Tenant.builder().id(2L).name("t2").build();

        when(tenantProvider.getCurrentTenantId()).thenReturn(2L);
        when(tenantRepository.findById(2L)).thenReturn(Optional.of(tenant));
        when(passwordEncoder.encode("secret")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User saved = userService.createUser(request);

        assertThat(saved.getPasswordHash()).isEqualTo("hashed");
    }

    @Test
    void createUserUsesPlainPasswordWhenEncoderMissing() {
        CreateUserRequestDto request = new CreateUserRequestDto();
        request.setUsername("alex");
        request.setEmail("alex@example.com");
        request.setPassword("secret");

        Tenant tenant = Tenant.builder().id(2L).name("t2").build();

        UserServiceImpl localService = new UserServiceImpl(userRepository, tenantRepository, tenantProvider);

        when(tenantProvider.getCurrentTenantId()).thenReturn(2L);
        when(tenantRepository.findById(2L)).thenReturn(Optional.of(tenant));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User saved = localService.createUser(request);

        assertThat(saved.getPasswordHash()).isEqualTo("secret");
    }

    @Test
    void createUserUsesProvidedTenantIdWhenPresent() {
        CreateUserRequestDto request = new CreateUserRequestDto();
        request.setTenantId(9L);
        request.setUsername("alex");
        request.setEmail("alex@example.com");
        request.setPassword("secret");

        Tenant tenant = Tenant.builder().id(9L).name("t9").build();

        when(tenantRepository.findById(9L)).thenReturn(Optional.of(tenant));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User saved = userService.createUser(request);

        assertThat(saved.getTenant()).isEqualTo(tenant);
        verifyNoInteractions(tenantProvider);
    }

    @Test
    void getAllUsersReturnsList() {
        User user = User.builder().username("alex").build();

        when(tenantProvider.getCurrentTenantId()).thenReturn(1L);
        when(userRepository.findAllByTenant_Id(1L)).thenReturn(List.of(user));

        List<User> users = userService.getAllUsers();

        assertThat(users).containsExactly(user);
    }

    @Test
    void getUserByIdReturnsOptional() {
        User user = User.builder().id(5L).username("alex").build();

        when(tenantProvider.getCurrentTenantId()).thenReturn(1L);
        when(userRepository.findByIdAndTenant_Id(5L, 1L)).thenReturn(Optional.of(user));

        Optional<User> result = userService.getUserById(5L);

        assertThat(result).contains(user);
    }

    @Test
    void updateUserUpdatesFields() {
        User existing = User.builder().id(1L).username("old").email("old@example.com").build();
        UpdateUserRequestDto update = new UpdateUserRequestDto();
        update.setUsername("new");
        update.setEmail("new@example.com");

        when(tenantProvider.getCurrentTenantId()).thenReturn(1L);
        when(userRepository.findByIdAndTenant_Id(1L, 1L)).thenReturn(Optional.of(existing));

        User result = userService.updateUser(1L, update);

        assertThat(result.getUsername()).isEqualTo("new");
        assertThat(result.getEmail()).isEqualTo("new@example.com");
    }

    @Test
    void updateUserThrowsWhenMissing() {
        when(tenantProvider.getCurrentTenantId()).thenReturn(1L);
        when(userRepository.findByIdAndTenant_Id(9L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(9L, new UpdateUserRequestDto()))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void deleteUserThrowsWhenMissing() {
        when(tenantProvider.getCurrentTenantId()).thenReturn(1L);
        when(userRepository.existsByIdAndTenant_Id(7L, 1L)).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteUser(7L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void deleteUserRemovesExisting() {
        when(tenantProvider.getCurrentTenantId()).thenReturn(1L);
        when(userRepository.existsByIdAndTenant_Id(8L, 1L)).thenReturn(true);

        userService.deleteUser(8L);

        verify(userRepository).deleteById(8L);
    }
}
