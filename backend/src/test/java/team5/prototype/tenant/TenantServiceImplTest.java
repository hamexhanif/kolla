package team5.prototype.tenant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team5.prototype.security.TenantProvider;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantServiceImplTest {

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private TenantProvider tenantProvider;

    @InjectMocks
    private TenantServiceImpl tenantService;

    @Test
    void createTenantSavesEntity() {
        Tenant tenant = Tenant.builder()
                .name("Tenant A")
                .subdomain("tenant-a")
                .active(true)
                .build();

        when(tenantRepository.save(tenant)).thenReturn(tenant);

        Tenant saved = tenantService.createTenant(tenant);

        assertThat(saved).isEqualTo(tenant);
    }

    @Test
    void getAllTenantsReturnsList() {
        Tenant tenant = Tenant.builder()
                .name("Tenant A")
                .subdomain("tenant-a")
                .active(true)
                .build();

        when(tenantProvider.getCurrentTenantId()).thenReturn(3L);
        when(tenantRepository.findById(3L)).thenReturn(Optional.of(tenant));

        List<Tenant> result = tenantService.getAllTenants();

        assertThat(result).containsExactly(tenant);
    }

    @Test
    void getTenantByIdReturnsOptional() {
        Tenant tenant = Tenant.builder()
                .id(3L)
                .name("Tenant A")
                .subdomain("tenant-a")
                .active(true)
                .build();

        when(tenantProvider.getCurrentTenantId()).thenReturn(3L);
        when(tenantRepository.findById(3L)).thenReturn(Optional.of(tenant));

        Optional<Tenant> result = tenantService.getTenantById(3L);

        assertThat(result).contains(tenant);
    }

    @Test
    void getTenantByIdReturnsEmptyWhenTenantMismatch() {
        when(tenantProvider.getCurrentTenantId()).thenReturn(1L);

        Optional<Tenant> result = tenantService.getTenantById(2L);

        assertThat(result).isEmpty();
        verify(tenantRepository, never()).findById(2L);
    }

    @Test
    void updateTenantThrowsWhenMissing() {
        when(tenantProvider.getCurrentTenantId()).thenReturn(7L);
        when(tenantRepository.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tenantService.updateTenant(7L, new Tenant()))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void updateTenantThrowsWhenTenantMismatch() {
        when(tenantProvider.getCurrentTenantId()).thenReturn(1L);

        assertThatThrownBy(() -> tenantService.updateTenant(2L, new Tenant()))
                .isInstanceOf(RuntimeException.class);
        verify(tenantRepository, never()).findById(2L);
    }

    @Test
    void updateTenantUpdatesFields() {
        Tenant existing = Tenant.builder()
                .id(5L)
                .name("Old")
                .subdomain("old")
                .active(true)
                .build();
        Tenant update = Tenant.builder()
                .name("New")
                .subdomain("new")
                .active(false)
                .build();

        when(tenantProvider.getCurrentTenantId()).thenReturn(5L);
        when(tenantRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(tenantRepository.save(any(Tenant.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Tenant result = tenantService.updateTenant(5L, update);

        assertThat(result.getName()).isEqualTo("New");
        assertThat(result.getSubdomain()).isEqualTo("new");
        assertThat(result.getActive()).isFalse();
    }

    @Test
    void deleteTenantThrowsWhenMissing() {
        when(tenantProvider.getCurrentTenantId()).thenReturn(9L);
        when(tenantRepository.existsById(9L)).thenReturn(false);

        assertThatThrownBy(() -> tenantService.deleteTenant(9L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void deleteTenantThrowsWhenTenantMismatch() {
        when(tenantProvider.getCurrentTenantId()).thenReturn(1L);

        assertThatThrownBy(() -> tenantService.deleteTenant(2L))
                .isInstanceOf(RuntimeException.class);
        verify(tenantRepository, never()).existsById(2L);
    }

    @Test
    void deleteTenantRemovesExisting() {
        when(tenantProvider.getCurrentTenantId()).thenReturn(10L);
        when(tenantRepository.existsById(10L)).thenReturn(true);

        tenantService.deleteTenant(10L);

        verify(tenantRepository).deleteById(10L);
    }
}
