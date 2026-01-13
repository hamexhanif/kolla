package team5.prototype.role;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import team5.prototype.dto.CreateRoleRequestDto;
import team5.prototype.tenant.Tenant;
import team5.prototype.tenant.TenantRepository;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class RoleServiceIntegrationTest {

    @Autowired
    private RoleService roleService;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void createRolePersistsTenantRelation() {
        String suffix = String.valueOf(System.nanoTime());
        Tenant tenant = Tenant.builder()
                .name("tenant-" + suffix)
                .subdomain("sub-" + suffix)
                .active(true)
                .build();
        tenant = tenantRepository.save(tenant);

        CreateRoleRequestDto request = new CreateRoleRequestDto();
        request.setTenantId(tenant.getId());
        request.setName("role-" + suffix);
        request.setDescription("desc");

        Role created = roleService.createRole(request);

        assertThat(created.getId()).isNotNull();
        Role persisted = roleRepository.findById(created.getId()).orElseThrow();
        assertThat(persisted.getTenant().getId()).isEqualTo(tenant.getId());
    }
}
