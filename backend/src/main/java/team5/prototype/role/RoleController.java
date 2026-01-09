package team5.prototype.role;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import team5.prototype.dto.RoleDto;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping
    public Role createRole(@RequestBody Role role) {
        return roleService.createRole(role);
    }

    @GetMapping
    public List<RoleDto> getAllRoles() {
        return roleService.getAllRolesAsDto();
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoleDto> getRoleById(@PathVariable Long id) {
        RoleDto roleDto = roleService.getRoleByIdAsDto(id);
        if (roleDto != null) {
            return ResponseEntity.ok(roleDto);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }
}