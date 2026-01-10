package team5.prototype.role;

import org.springframework.stereotype.Service;
import team5.prototype.security.TenantProvider;
import team5.prototype.tenant.Tenant;
import team5.prototype.tenant.TenantRepository;
import team5.prototype.user.User;
import team5.prototype.user.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final TenantProvider tenantProvider;

    public RoleServiceImpl(RoleRepository roleRepository,
                           UserRepository userRepository,
                           TenantRepository tenantRepository,
                           TenantProvider tenantProvider) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.tenantProvider = tenantProvider;
    }

    @Override
    public Role createRole(Role role) {
        Long tenantId = tenantProvider.getCurrentTenantId();
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant mit ID " + tenantId + " nicht gefunden!"));
        role.setTenant(tenant);
        return roleRepository.save(role);
    }

    @Override
    public List<Role> getAllRoles() {
        return roleRepository.findAllByTenant_Id(tenantProvider.getCurrentTenantId());
    }

    @Override
    public Optional<Role> getRoleById(Long roleId) {
        return roleRepository.findByIdAndTenant_Id(roleId, tenantProvider.getCurrentTenantId());
    }

    @Override
    public Role updateRole(Long roleId, Role roleDetails) {
        return roleRepository.findByIdAndTenant_Id(roleId, tenantProvider.getCurrentTenantId())
                .map(existingRole -> {
                    existingRole.setName(roleDetails.getName());
                    existingRole.setDescription(roleDetails.getDescription());
                    return roleRepository.save(existingRole);
                })
                .orElseThrow(() -> new RuntimeException("Rolle mit ID " + roleId + " nicht gefunden!"));
    }

    @Override
    public void deleteRole(Long roleId) {
        Role role = roleRepository.findByIdAndTenant_Id(roleId, tenantProvider.getCurrentTenantId())
                .orElseThrow(() -> new RuntimeException("Rolle mit ID " + roleId + " nicht gefunden!"));
        roleRepository.delete(role);
    }

    @Override
    public void assignRoleToUser(Long userId, Long roleId) {
        Long tenantId = tenantProvider.getCurrentTenantId();
        User user = userRepository.findByIdAndTenant_Id(userId, tenantId)
                .orElseThrow(() -> new RuntimeException("User mit ID " + userId + " nicht gefunden!"));

        Role role = roleRepository.findByIdAndTenant_Id(roleId, tenantId)
                .orElseThrow(() -> new RuntimeException("Rolle mit ID " + roleId + " nicht gefunden!"));

        user.getRoles().add(role);
        userRepository.save(user);
    }
}
