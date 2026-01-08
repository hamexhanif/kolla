package team5.prototype.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import team5.prototype.role.Role;
import team5.prototype.tenant.Tenant;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    @Test
    void findByUsernameReturnsUser() {
        Tenant tenant = persistTenant("t1");
        User user = User.builder()
                .username("alex")
                .email("alex@example.com")
                .passwordHash("hash")
                .active(true)
                .tenant(tenant)
                .build();
        entityManager.persist(user);
        entityManager.flush();

        Optional<User> result = userRepository.findByUsername("alex");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("alex@example.com");
    }

    @Test
    void findFirstByRolesNameAndTenantIdReturnsMatch() {
        Tenant tenant = persistTenant("t2");
        Role role = Role.builder()
                .name("REVIEWER")
                .tenant(tenant)
                .build();
        entityManager.persist(role);
        User user = User.builder()
                .username("reviewer")
                .email("reviewer@example.com")
                .passwordHash("hash")
                .active(true)
                .tenant(tenant)
                .build();
        user.getRoles().add(role);
        entityManager.persist(user);
        entityManager.flush();

        Optional<User> result = userRepository.findFirstByRoles_NameAndTenant_IdOrderByIdAsc("REVIEWER", tenant.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("reviewer");
    }

    @Test
    void findFirstByRolesNameAndTenantIdReturnsEmptyForOtherTenant() {
        Tenant tenant = persistTenant("t3");
        Tenant otherTenant = persistTenant("t4");
        Role role = Role.builder()
                .name("ADMIN")
                .tenant(tenant)
                .build();
        entityManager.persist(role);
        User user = User.builder()
                .username("admin")
                .email("admin@example.com")
                .passwordHash("hash")
                .active(true)
                .tenant(tenant)
                .build();
        user.getRoles().add(role);
        entityManager.persist(user);
        entityManager.flush();

        Optional<User> result = userRepository.findFirstByRoles_NameAndTenant_IdOrderByIdAsc("ADMIN", otherTenant.getId());

        assertThat(result).isEmpty();
    }

    private Tenant persistTenant(String subdomain) {
        Tenant tenant = Tenant.builder()
                .name("Tenant " + subdomain)
                .subdomain(subdomain)
                .active(true)
                .build();
        entityManager.persist(tenant);
        return tenant;
    }
}
