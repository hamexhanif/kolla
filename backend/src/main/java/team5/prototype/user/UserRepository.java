package team5.prototype.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByUsernameAndTenant_Id(String username, Long tenantId);
    Optional<User> findByEmail(String email);

    Optional<User> findFirstByRoles_NameAndTenant_IdOrderByIdAsc(String roleName, Long tenantId);

    List<User> findAllByTenant_Id(Long tenantId);

    Optional<User> findByIdAndTenant_Id(Long id, Long tenantId);

    boolean existsByIdAndTenant_Id(Long id, Long tenantId);
}
