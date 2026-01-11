package team5.prototype.tenant;
import java.util.List;
import java.util.Optional;
public interface TenantService {
    Tenant createTenant(TenantDto requestDto);
    List<Tenant> getAllTenants();
    Optional<Tenant> getTenantById(Long id);
    void deleteTenant(Long id);
}