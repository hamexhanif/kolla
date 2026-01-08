package team5.prototype.tenant;

import org.springframework.stereotype.Service;
import team5.prototype.security.TenantProvider;

import java.util.List;
import java.util.Optional;

@Service
public class TenantServiceImpl implements TenantService{

    private final TenantRepository tenantRepository;
    private final TenantProvider tenantProvider;

    public TenantServiceImpl(TenantRepository tenantRepository, TenantProvider tenantProvider) {
        this.tenantRepository = tenantRepository;
        this.tenantProvider = tenantProvider;
    }

    @Override
    public Tenant createTenant(Tenant tenant) {
        // Tenant-Erstellung ist ein Admin-Vorgang. Aktuell keine Sonderrollen vorhanden,
        // daher lassen wir die Erstellung weiterhin zu.
        return tenantRepository.save(tenant);
    }

    @Override
    public List<Tenant> getAllTenants() {
        Long tenantId = tenantProvider.getCurrentTenantId();
        return tenantRepository.findById(tenantId).map(List::of).orElseGet(List::of);
    }

    @Override
    public Optional<Tenant> getTenantById(Long tenantId) {
        Long currentTenantId = tenantProvider.getCurrentTenantId();
        if (!tenantId.equals(currentTenantId)) {
            return Optional.empty();
        }
        return tenantRepository.findById(tenantId);
    }

    @Override
    public Tenant updateTenant(Long tenantId, Tenant tenantDetails) {
        Long currentTenantId = tenantProvider.getCurrentTenantId();
        if (!tenantId.equals(currentTenantId)) {
            throw new RuntimeException("Tenant mit ID " + tenantId + " nicht gefunden!");
        }
        return tenantRepository.findById(tenantId)
                .map(existingTenant -> {
                    existingTenant.setName(tenantDetails.getName());
                    existingTenant.setSubdomain(tenantDetails.getSubdomain());
                    existingTenant.setActive(tenantDetails.getActive());
                    return tenantRepository.save(existingTenant);
                })
                .orElseThrow(() -> new RuntimeException("Tenant mit ID " + tenantId + " nicht gefunden!"));
    }

    @Override
    public void deleteTenant(Long tenantId) {
        Long currentTenantId = tenantProvider.getCurrentTenantId();
        if (!tenantId.equals(currentTenantId) || !tenantRepository.existsById(tenantId)) {
            throw new RuntimeException("Tenant mit ID " + tenantId + " nicht gefunden!");
        }
        tenantRepository.deleteById(tenantId);
    }
}
