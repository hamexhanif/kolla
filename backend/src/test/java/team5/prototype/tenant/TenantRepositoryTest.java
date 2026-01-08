package team5.prototype.tenant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class TenantRepositoryTest {

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void findByIdReturnsPersistedTenant() {
        Tenant tenant = Tenant.builder()
                .name("Tenant Repo")
                .subdomain("tenant-repo")
                .active(true)
                .build();
        entityManager.persist(tenant);
        entityManager.flush();

        Optional<Tenant> result = tenantRepository.findById(tenant.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getSubdomain()).isEqualTo("tenant-repo");
    }

    @Test
    void findAllReturnsMultipleTenants() {
        Tenant first = Tenant.builder()
                .name("Tenant One")
                .subdomain("tenant-one")
                .active(true)
                .build();
        Tenant second = Tenant.builder()
                .name("Tenant Two")
                .subdomain("tenant-two")
                .active(false)
                .build();
        entityManager.persist(first);
        entityManager.persist(second);
        entityManager.flush();

        assertThat(tenantRepository.findAll())
                .extracting(Tenant::getSubdomain)
                .contains("tenant-one", "tenant-two");
    }
}
