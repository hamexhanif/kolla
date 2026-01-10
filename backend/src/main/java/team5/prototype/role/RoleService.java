package team5.prototype.role;

import java.util.List;
import java.util.Optional;

public interface RoleService {
    Role createRole(Role role);
    List<Role> getAllRoles();
    Optional<Role> getRoleById(Long roleId);
    Role updateRole(Long roleId, Role roleDetails);
    void deleteRole(Long roleId);
    void assignRoleToUser(Long userId, Long roleId);
}
