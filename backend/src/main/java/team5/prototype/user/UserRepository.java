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
}
