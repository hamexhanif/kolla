package team5.prototype.tenant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TenantServiceImplTest {

    @Mock
    private TenantRepository tenantRepository;

    @InjectMocks
    private TenantServiceImpl tenantService;

    @Test
    void createTenantSetsActiveAndSaves() {
        TenantDto dto = TenantDto.builder().name("t").subdomain("s").build();
        when(tenantRepository.save(any(Tenant.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Tenant created = tenantService.createTenant(dto);

        assertThat(created.getName()).isEqualTo("t");
        assertThat(created.getActive()).isTrue();
    }

    @Test
    void deleteTenantThrowsWhenMissing() {
        when(tenantRepository.existsById(10L)).thenReturn(false);

        assertThatThrownBy(() -> tenantService.deleteTenant(10L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void getAllTenantsDelegatesToRepository() {
        Tenant tenant = Tenant.builder().id(1L).name("t").build();
        when(tenantRepository.findAll()).thenReturn(List.of(tenant));

        assertThat(tenantService.getAllTenants()).containsExactly(tenant);
    }
}
