package team5.prototype.tenant;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tenants")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @PostMapping
    public Tenant createTenant(@RequestBody Tenant request) {
        return tenantService.createTenant(request);
    }

    @GetMapping
    public List<Tenant> getAllTenants() {
        return tenantService.getAllTenants();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tenant> getTenantById(@PathVariable Long id) {
        Optional<Tenant> tenant = tenantService.getTenantById(id);
        return tenant.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Tenant> updateTenant(@PathVariable Long id, @RequestBody Tenant tenantDetails) {
        Tenant updated = tenantService.updateTenant(id, tenantDetails);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTenant(@PathVariable Long id) {
        tenantService.deleteTenant(id);
        return ResponseEntity.noContent().build();
    }
}
