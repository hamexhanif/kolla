package team5.prototype.role;

import team5.prototype.dto.CreateRoleRequestDto;

import java.util.List;

public interface RoleService {

    Role createRole(CreateRoleRequestDto requestDto);
    List<RoleDto> getAllRolesAsDto();
    RoleDto getRoleByIdAsDto(Long id);
    Role updateRole(Long roleId, Role roleDetails);
    void deleteRole(Long roleId);

    void assignRoleToUser(Long userId, Long roleId);
}