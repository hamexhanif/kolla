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

    /**
     * Find all active users with a specific role and tenant.
     * Results ordered by user ID for consistency.
     */
    @Query("SELECT DISTINCT u FROM User u " +
            "JOIN u.roles r " +
            "WHERE r.name = :roleName AND u.tenant.id = :tenantId AND u.active = true")
    List<User> findActiveUsersByRoleAndTenant(
            @Param("roleName") String roleName,
            @Param("tenantId") Long tenantId
    );

    @Query("SELECT u FROM User u WHERE u.active = true")
    List<User> findAllActive();

    @Query("SELECT u FROM User u WHERE u.id = :id AND u.active = true")
    Optional<User> findByIdAndActive(@Param("id") Long id);

    @Query("SELECT u FROM User u WHERE u.active = true AND u.tenant.id = :tenantId")
    List<User> findAllActiveByTenantId(@Param("tenantId") Long tenantId);

    @Query("SELECT u FROM User u WHERE u.id = :id AND u.tenant.id = :tenantId")
    Optional<User> findByIdAndTenantId(@Param("id") Long id, @Param("tenantId") Long tenantId);

    @Query("SELECT u FROM User u WHERE u.id = :id AND u.tenant.id = :tenantId AND u.active = true")
    Optional<User> findByIdAndTenantIdAndActive(@Param("id") Long id, @Param("tenantId") Long tenantId);
}
