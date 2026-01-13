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
    public List<Tenant> getAllTenants() { return tenantRepository.findAll(); }

    @Override
    public void deleteTenant(Long id) {
        if (!tenantRepository.existsById(id)) { throw new EntityNotFoundException("Tenant mit ID " + id + " nicht gefunden!"); }
        tenantRepository.deleteById(id);
    }
}
