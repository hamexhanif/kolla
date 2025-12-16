package team5.prototype.role;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Findet eine Rolle anhand ihres Namens innerhalb eines bestimmten Mandanten.
     * @param name Der Name der Rolle.
     * @param tenantId Die ID des Mandanten.
     * @return Ein Optional, das die Rolle enthält, falls gefunden.
     */
    Optional<Role> findByNameAndTenantId(String name, Long tenantId);
}