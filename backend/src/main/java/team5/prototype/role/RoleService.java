package team5.prototype.role;

import team5.prototype.dto.CreateRoleRequestDto;
import team5.prototype.dto.RoleDto;

import java.util.List;
import java.util.Optional;

public interface RoleService {
    // CRUD-Operationen
    Role createRole(CreateRoleRequestDto requestDto);
    List<Role> getAllRoles();
    Optional<Role> getRoleById(Long roleId);
    List<RoleDto> getAllRolesAsDto();
    RoleDto getRoleByIdAsDto(Long id);
    Role updateRole(Long roleId, Role roleDetails);
    void deleteRole(Long roleId);

    // Geschäftslogik
    void assignRoleToUser(Long userId, Long roleId);
}