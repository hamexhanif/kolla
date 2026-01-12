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
    void assignRoleToUserDelegates() {
        controller.assignRoleToUser(Map.of("userId", 1L, "roleId", 2L));

        verify(roleService).assignRoleToUser(1L, 2L);
    }

    @Test
    void getAllRolesDelegates() {
        when(roleService.getAllRolesAsDto()).thenReturn(List.of(new RoleDto()));

        assertThat(controller.getAllRoles()).hasSize(1);
    }
}
