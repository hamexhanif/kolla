package team5.prototype.role;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import team5.prototype.dto.CreateRoleRequestDto;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RoleControllerTest {

    @Mock
    private RoleService roleService;

    @InjectMocks
    private RoleController controller;

    @Test
    void createRoleMapsDto() {
        Role role = Role.builder().id(1L).name("r").description("d").build();
        when(roleService.createRole(org.mockito.ArgumentMatchers.any(CreateRoleRequestDto.class)))
                .thenReturn(role);

        RoleDto dto = controller.createRole(new CreateRoleRequestDto());

        assertThat(dto.getId()).isEqualTo(1L);
    }

    @Test
    void getRoleByIdNotFound() {
        when(roleService.getRoleByIdAsDto(10L)).thenReturn(null);

        ResponseEntity<RoleDto> response = controller.getRoleById(10L);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void getRoleByIdReturnsOk() {
        RoleDto dto = RoleDto.builder().id(10L).name("r").build();
        when(roleService.getRoleByIdAsDto(10L)).thenReturn(dto);

        ResponseEntity<RoleDto> response = controller.getRoleById(10L);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(dto);
    }

    @Test
    void assignRoleToUserDelegates() {
        controller.assignRoleToUser(Map.of("userId", 1L, "roleId", 2L));

        verify(roleService).assignRoleToUser(1L, 2L);
    }

    @Test
    void updateRoleMapsAndReturnsDto() {
        RoleDto request = RoleDto.builder().name("new").description("d").build();
        Role updatedRole = Role.builder().id(7L).name("new").description("d").build();
        when(roleService.updateRole(eq(7L), any(Role.class))).thenReturn(updatedRole);

        ResponseEntity<RoleDto> response = controller.updateRole(7L, request);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().getId()).isEqualTo(7L);
        assertThat(response.getBody().getName()).isEqualTo("new");
    }

    @Test
    void deleteRoleDelegatesAndReturnsNoContent() {
        ResponseEntity<Void> response = controller.deleteRole(5L);

        assertThat(response.getStatusCode().value()).isEqualTo(204);
        verify(roleService).deleteRole(5L);
    }

    @Test
    void getAllRolesDelegates() {
        when(roleService.getAllRolesAsDto()).thenReturn(List.of(new RoleDto()));

        assertThat(controller.getAllRoles()).hasSize(1);
    }
}
