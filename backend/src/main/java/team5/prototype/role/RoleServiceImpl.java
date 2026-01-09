package team5.prototype.role;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team5.prototype.dto.RoleDto;
import team5.prototype.user.User; // Import der User-Klasse
import team5.prototype.user.UserRepository; // Import des UserRepository

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository; // Wird für assignRoleToUser benötigt

    public RoleServiceImpl(RoleRepository roleRepository, UserRepository userRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Role createRole(Role role) {
        // Hier könnten Validierungen für Tenant, etc. hinzukommen
        return roleRepository.save(role);
    }

    @Override
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @Override
    public Optional<Role> getRoleById(Long roleId) {
        return roleRepository.findById(roleId);
    }

    @Transactional(readOnly = true)
    @Override
    public List<RoleDto> getAllRolesAsDto() {
        return roleRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public RoleDto getRoleByIdAsDto(Long id) {
        return roleRepository.findById(id)
                .map(this::convertToDto)
                .orElse(null);
    }

    private RoleDto convertToDto(Role role) {
        return RoleDto.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .tenantName(role.getTenant() != null ? role.getTenant().getName() : null)
                .build();
    }

    @Override
    public Role updateRole(Long roleId, Role roleDetails) {
        return roleRepository.findById(roleId)
                .map(existingRole -> {
                    existingRole.setName(roleDetails.getName());
                    existingRole.setDescription(roleDetails.getDescription());
                    return roleRepository.save(existingRole);
                })
                .orElseThrow(() -> new RuntimeException("Rolle mit ID " + roleId + " nicht gefunden!"));
    }

    @Override
    public void deleteRole(Long roleId) {
        roleRepository.deleteById(roleId);
    }

    @Override
    public void assignRoleToUser(Long userId, Long roleId) {
        // Lade den User und die Rolle aus der Datenbank
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User mit ID " + userId + " nicht gefunden!"));

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Rolle mit ID " + roleId + " nicht gefunden!"));

        // Führe die Zuweisung durch (in der User-Entity)
        user.getRoles().add(role); // Annahme: User hat eine Set<Role> roles

        userRepository.save(user);
    }
}