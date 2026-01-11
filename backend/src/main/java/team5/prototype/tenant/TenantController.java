package team5.prototype.tenant;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tenants")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @PostMapping
    public TenantDto createTenant(@RequestBody TenantDto requestDto) { // SIGNATUR GEÄNDERT
        Tenant createdTenant = tenantService.createTenant(requestDto);
        return convertToDto(createdTenant);
    }

    @GetMapping
    public List<TenantDto> getAllTenants() {
        return tenantService.getAllTenants().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTenant(@PathVariable Long id) {
        tenantService.deleteTenant(id);
        return ResponseEntity.noContent().build();
    }

    private TenantDto convertToDto(Tenant tenant) {
        return TenantDto.builder()
                .id(tenant.getId())
                .name(tenant.getName())
                .subdomain(tenant.getSubdomain())
                .active(tenant.getActive())
                .createdAt(tenant.getCreatedAt()) // Wir können das createdAt-Feld auch mitsenden
                .build();
    }
}