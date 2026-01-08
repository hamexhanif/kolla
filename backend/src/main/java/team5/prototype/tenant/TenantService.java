package team5.prototype.tenant;

public interface TenantService {
    Tenant createTenant(Tenant tenant);

    java.util.List<Tenant> getAllTenants();

    java.util.Optional<Tenant> getTenantById(Long tenantId);

    Tenant updateTenant(Long tenantId, Tenant tenantDetails);

    void deleteTenant(Long tenantId);
}
