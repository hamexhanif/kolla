package team5.prototype.tenant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TenantControllerTest {

    @Mock
    private TenantService tenantService;

    @InjectMocks
    private TenantController controller;

    @Test
    void createTenantMapsDto() {
        Tenant tenant = Tenant.builder().id(1L).name("t").subdomain("s").active(true).build();
        when(tenantService.createTenant(org.mockito.ArgumentMatchers.any(TenantDto.class)))
                .thenReturn(tenant);

        TenantDto dto = controller.createTenant(TenantDto.builder().name("t").build());

        assertThat(dto.getId()).isEqualTo(1L);
    }

    @Test
    void getAllTenantsMapsList() {
        Tenant tenant = Tenant.builder().id(1L).name("t").build();
        when(tenantService.getAllTenants()).thenReturn(List.of(tenant));

        List<TenantDto> result = controller.getAllTenants();

        assertThat(result).hasSize(1);
    }

    @Test
    void deleteTenantDelegates() {
        ResponseEntity<Void> response = controller.deleteTenant(5L);

        assertThat(response.getStatusCode().value()).isEqualTo(204);
        verify(tenantService).deleteTenant(5L);
    }
}
