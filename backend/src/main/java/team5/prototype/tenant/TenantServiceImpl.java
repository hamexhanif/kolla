package team5.prototype.tenant;
import org.springframework.stereotype.Service;
import team5.prototype.dto.TenantDto;
import java.util.List;
import java.util.Optional;
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
    public Optional<Tenant> getTenantById(Long id) { return tenantRepository.findById(id); }
    @Override
    public void deleteTenant(Long id) {
        if (!tenantRepository.existsById(id)) { throw new RuntimeException("..."); }
        tenantRepository.deleteById(id);
    }
}