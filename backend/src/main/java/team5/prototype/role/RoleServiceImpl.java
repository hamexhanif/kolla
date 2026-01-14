package team5.prototype.role;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team5.prototype.dto.CreateRoleRequestDto;
import team5.prototype.tenant.Tenant;
import team5.prototype.tenant.TenantContext;
import team5.prototype.tenant.TenantRepository;
import team5.prototype.user.User; // Import der User-Klasse
import team5.prototype.user.UserRepository; // Import des UserRepository

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository; // Wird für assignRoleToUser benötigt

    public RoleServiceImpl(RoleRepository roleRepository, UserRepository userRepository, TenantRepository tenantRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
    }

    @Override
    public Role createRole(CreateRoleRequestDto requestDto) {
        Long tenantId = currentTenantId();
        if (requestDto.getTenantId() != null && !tenantId.equals(requestDto.getTenantId())) {
            throw new RuntimeException("Tenant mismatch");
        }
        // Lade den Tenant anhand der ID aus dem DTO
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant nicht gefunden"));

        Role newRole = new Role();
        newRole.setName(requestDto.getName());
        newRole.setDescription(requestDto.getDescription());
        newRole.setTenant(tenant);// WICHTIG: Setze den Tenant

        return roleRepository.save(newRole);
    }

    @Transactional(readOnly = true)
    @Override
    public List<RoleDto> getAllRolesAsDto() {
        return roleRepository.findAllByTenantId(currentTenantId()).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public RoleDto getRoleByIdAsDto(Long id) {
        return roleRepository.findByIdAndTenantId(id, currentTenantId())
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
        return roleRepository.findByIdAndTenantId(roleId, currentTenantId())
                .map(existingRole -> {
                    existingRole.setName(roleDetails.getName());
                    existingRole.setDescription(roleDetails.getDescription());
                    return roleRepository.save(existingRole);
                })
                .orElseThrow(() -> new RuntimeException("Rolle mit ID " + roleId + " nicht gefunden!"));
    }

    @Override
    public void deleteRole(Long roleId) {
        Role existing = roleRepository.findByIdAndTenantId(roleId, currentTenantId())
                .orElseThrow(() -> new RuntimeException("Rolle mit ID " + roleId + " nicht gefunden!"));
        roleRepository.deleteById(existing.getId());
    }

    @Override
    public void assignRoleToUser(Long userId, Long roleId) {
        Long tenantId = currentTenantId();
        // Lade den User und die Rolle aus der Datenbank
        User user = userRepository.findByIdAndTenantId(userId, tenantId)
                .orElseThrow(() -> new RuntimeException("User mit ID " + userId + " nicht gefunden!"));

        Role role = roleRepository.findByIdAndTenantId(roleId, tenantId)
                .orElseThrow(() -> new RuntimeException("Rolle mit ID " + roleId + " nicht gefunden!"));

        // Führe die Zuweisung durch (in der User-Entity)
        user.getRoles().add(role); // Annahme: User hat eine Set<Role> roles

        userRepository.save(user);
    }

    private Long currentTenantId() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new RuntimeException("Kein Tenant-Kontext vorhanden");
        }
        return tenantId;
    }
}
