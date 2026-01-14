package team5.prototype.tenant;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TenantServiceImpl implements TenantService {
    private final TenantRepository tenantRepository;
    public TenantServiceImpl(TenantRepository tenantRepository) { this.tenantRepository = tenantRepository; }

    @Override
    public Tenant createTenant(TenantDto requestDto) {
        Tenant newTenant = Tenant.builder().name(requestDto.getName()).subdomain(requestDto.getSubdomain()).active(true).build();
        return tenantRepository.save(newTenant);
    }

    @Override
    public List<Tenant> getAllTenants() {
        Long tenantId = currentTenantId();
        return tenantRepository.findById(tenantId).map(List::of).orElseGet(List::of);
    }
    @Override
    public void deleteTenant(Long id) {
        if (!currentTenantId().equals(id)) { throw new RuntimeException("..."); }
        if (!tenantRepository.existsById(id)) { throw new RuntimeException("..."); }
        tenantRepository.deleteById(id);
    }

    private Long currentTenantId() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new RuntimeException("Kein Tenant-Kontext vorhanden");
        }
        return tenantId;
    }
}
