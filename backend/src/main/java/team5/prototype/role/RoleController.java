package team5.prototype.role;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import team5.prototype.dto.CreateRoleRequestDto;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping
    public RoleDto createRole(@RequestBody CreateRoleRequestDto requestDto) {
        Role createdRole = roleService.createRole(requestDto);
        return convertToDto(createdRole);
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
    @PostMapping("/assign")
    public ResponseEntity<Void> assignRoleToUser(@RequestBody Map<String, Long> request) {
        Long userId = request.get("userId");
        Long roleId = request.get("roleId");
        roleService.assignRoleToUser(userId, roleId);
        return ResponseEntity.ok().build();
    }
    @PutMapping("/{id}")
    public ResponseEntity<RoleDto> updateRole(@PathVariable Long id, @RequestBody RoleDto roleDetails) {
        Role roleToUpdate = new Role();
        roleToUpdate.setName(roleDetails.getName());
        roleToUpdate.setDescription(roleDetails.getDescription());

        Role updatedRole = roleService.updateRole(id, roleToUpdate);
        return ResponseEntity.ok(convertToDto(updatedRole));
    }
    private RoleDto convertToDto(Role role) {
        return RoleDto.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .build();
    }
}