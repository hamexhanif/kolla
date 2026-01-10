package team5.prototype.tenant;

import java.util.List;
import java.util.Optional;

public interface TenantService {
    Tenant createTenant(Tenant tenant);
    List<Tenant> getAllTenants();
    Optional<Tenant> getTenantById(Long tenantId);
    Tenant updateTenant(Long tenantId, Tenant tenantDetails);
    void deleteTenant(Long tenantId);
}
