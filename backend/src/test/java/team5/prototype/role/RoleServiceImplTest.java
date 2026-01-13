package team5.prototype.role;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team5.prototype.dto.CreateRoleRequestDto;
import team5.prototype.tenant.Tenant;
import team5.prototype.tenant.TenantRepository;
import team5.prototype.user.User;
import team5.prototype.user.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;
    @Mock
    private TenantRepository tenantRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RoleServiceImpl roleService;

    @Test
    void createRoleUsesTenantAndSaves() {
        Tenant tenant = Tenant.builder().id(5L).name("t").build();
        CreateRoleRequestDto requestDto = new CreateRoleRequestDto();
        requestDto.setTenantId(tenant.getId());
        requestDto.setName("admin");
        requestDto.setDescription("d");

        when(tenantRepository.findById(tenant.getId())).thenReturn(Optional.of(tenant));
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Role created = roleService.createRole(requestDto);

        assertThat(created.getTenant()).isEqualTo(tenant);
        assertThat(created.getName()).isEqualTo("admin");
    }

    @Test
    void createRoleThrowsWhenTenantMissing() {
        CreateRoleRequestDto requestDto = new CreateRoleRequestDto();
        requestDto.setTenantId(99L);
        requestDto.setName("admin");

        when(tenantRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.createRole(requestDto))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void updateRoleThrowsWhenMissing() {
        when(roleRepository.findById(77L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.updateRole(77L, new Role()))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void updateRoleUpdatesAndSaves() {
        Role existing = Role.builder().id(7L).name("old").description("old").build();
        Role details = new Role();
        details.setName("new");
        details.setDescription("new");

        when(roleRepository.findById(7L)).thenReturn(Optional.of(existing));
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Role updated = roleService.updateRole(7L, details);

        assertThat(updated.getName()).isEqualTo("new");
        assertThat(updated.getDescription()).isEqualTo("new");
    }

    @Test
    void getAllRolesAsDtoMapsTenantName() {
        Tenant tenant = Tenant.builder().name("t").build();
        Role role = Role.builder().id(1L).name("r").description("d").tenant(tenant).build();
        when(roleRepository.findAll()).thenReturn(List.of(role));

        List<RoleDto> result = roleService.getAllRolesAsDto();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTenantName()).isEqualTo("t");
    }

    @Test
    void getRoleByIdAsDtoReturnsNullWhenMissing() {
        when(roleRepository.findById(404L)).thenReturn(Optional.empty());

        RoleDto result = roleService.getRoleByIdAsDto(404L);

        assertThat(result).isNull();
    }

    @Test
    void assignRoleToUserUpdatesUserRoles() {
        User user = User.builder().id(10L).roles(new java.util.HashSet<>()).build();
        Role role = Role.builder().id(20L).name("r").build();

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(roleRepository.findById(20L)).thenReturn(Optional.of(role));

        roleService.assignRoleToUser(10L, 20L);

        assertThat(user.getRoles()).contains(role);
        verify(userRepository).save(user);
    }

    @Test
    void assignRoleToUserThrowsWhenUserMissing() {
        when(userRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.assignRoleToUser(10L, 20L))
                .isInstanceOf(RuntimeException.class);
        verify(roleRepository, never()).findById(20L);
    }

    @Test
    void assignRoleToUserThrowsWhenRoleMissing() {
        User user = User.builder().id(10L).roles(new java.util.HashSet<>()).build();
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(roleRepository.findById(20L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.assignRoleToUser(10L, 20L))
                .isInstanceOf(RuntimeException.class);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteRoleDelegatesToRepository() {
        roleService.deleteRole(12L);

        verify(roleRepository).deleteById(12L);
    }
}
