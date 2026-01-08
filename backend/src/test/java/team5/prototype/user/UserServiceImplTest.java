package team5.prototype.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team5.prototype.security.TenantProvider;
import team5.prototype.tenant.Tenant;
import team5.prototype.tenant.TenantRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private TenantProvider tenantProvider;

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
    void getAllUsersReturnsList() {
        User user = User.builder().username("alex").build();

        when(tenantProvider.getCurrentTenantId()).thenReturn(1L);
        when(userRepository.findAllByTenant_Id(1L)).thenReturn(List.of(user));

        List<User> users = userService.getAllUsers();

        assertThat(users).containsExactly(user);
    }

    @Test
    void updateUserUpdatesFields() {
        User existing = User.builder().id(1L).username("old").email("old@example.com").build();
        User update = User.builder().username("new").email("new@example.com").build();

        when(tenantProvider.getCurrentTenantId()).thenReturn(1L);
        when(userRepository.findByIdAndTenant_Id(1L, 1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateUser(1L, update);

        assertThat(result.getUsername()).isEqualTo("new");
        assertThat(result.getEmail()).isEqualTo("new@example.com");
    }

    @Test
    void updateUserThrowsWhenMissing() {
        when(tenantProvider.getCurrentTenantId()).thenReturn(1L);
        when(userRepository.findByIdAndTenant_Id(9L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(9L, new User()))
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
