package team5.prototype.tenant;
import java.util.List;

public interface TenantService {
    Tenant createTenant(TenantDto requestDto);
    List<Tenant> getAllTenants();
    void deleteTenant(Long id);
}