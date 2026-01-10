package team5.prototype.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findFirstByRoles_NameAndTenant_IdOrderByIdAsc(String roleName, Long tenantId);

    Optional<User> findByIdAndTenant_Id(Long id, Long tenantId);
    Optional<User> findByUsernameAndTenant_Id(String username, Long tenantId);
    Optional<User> findByEmailAndTenant_Id(String email, Long tenantId);
    List<User> findAllByTenant_Id(Long tenantId);
    boolean existsByIdAndTenant_Id(Long id, Long tenantId);

    @Query("SELECT u FROM User u JOIN u.roles r " +
            "WHERE r.name = :roleName " +
            "AND u.active = true " +
            "AND u.tenant.id = :tenantId")
    List<User> findActiveUsersByRoleAndTenant(@Param("roleName") String roleName,
                                              @Param("tenantId") Long tenantId);
}
