package team5.prototype.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import team5.prototype.entity.Tenant;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {
    // Vorerst leer.
}