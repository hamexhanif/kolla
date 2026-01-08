package team5.prototype.role;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team5.prototype.security.TenantProvider;
import team5.prototype.tenant.Tenant;
import team5.prototype.tenant.TenantRepository;
import team5.prototype.user.User;
import team5.prototype.user.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private TenantProvider tenantProvider;

    @InjectMocks
    private RoleServiceImpl roleService;

    @Test
    void createRoleSavesEntity() {
        Role role = new Role();
        role.setName("ADMIN");
        Tenant tenant = Tenant.builder().id(1L).name("t1").build();

        when(tenantProvider.getCurrentTenantId()).thenReturn(1L);
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant));
        when(roleRepository.save(role)).thenReturn(role);

        Role saved = roleService.createRole(role);

        assertThat(saved).isEqualTo(role);
    }

    @Test
    void getAllRolesReturnsList() {
        Role role = new Role();
        role.setName("REVIEWER");

        when(tenantProvider.getCurrentTenantId()).thenReturn(1L);
        when(roleRepository.findAllByTenant_Id(1L)).thenReturn(List.of(role));

        List<Role> roles = roleService.getAllRoles();

        assertThat(roles).containsExactly(role);
    }

    @Test
    void updateRoleThrowsWhenMissing() {
        when(tenantProvider.getCurrentTenantId()).thenReturn(1L);
        when(roleRepository.findByIdAndTenant_Id(4L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.updateRole(4L, new Role()))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void updateRoleUpdatesFields() {
        Role existing = new Role();
        existing.setId(3L);
        existing.setName("OLD");
        existing.setDescription("old");

        Role update = new Role();
        update.setName("NEW");
        update.setDescription("new");

        when(tenantProvider.getCurrentTenantId()).thenReturn(1L);
        when(roleRepository.findByIdAndTenant_Id(3L, 1L)).thenReturn(Optional.of(existing));
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Role result = roleService.updateRole(3L, update);

        assertThat(result.getName()).isEqualTo("NEW");
        assertThat(result.getDescription()).isEqualTo("new");
    }

    @Test
    void assignRoleToUserAddsRole() {
        User user = User.builder().id(5L).build();
        Role role = new Role();
        role.setId(7L);
        role.setName("ADMIN");

        when(tenantProvider.getCurrentTenantId()).thenReturn(1L);
        when(userRepository.findByIdAndTenant_Id(5L, 1L)).thenReturn(Optional.of(user));
        when(roleRepository.findByIdAndTenant_Id(7L, 1L)).thenReturn(Optional.of(role));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        roleService.assignRoleToUser(5L, 7L);

        assertThat(user.getRoles()).contains(role);
        verify(userRepository).save(user);
    }

    @Test
    void assignRoleToUserThrowsWhenRoleMissingForTenant() {
        User user = User.builder().id(5L).build();

        when(tenantProvider.getCurrentTenantId()).thenReturn(1L);
        when(userRepository.findByIdAndTenant_Id(5L, 1L)).thenReturn(Optional.of(user));
        when(roleRepository.findByIdAndTenant_Id(7L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.assignRoleToUser(5L, 7L))
                .isInstanceOf(RuntimeException.class);
    }
}
