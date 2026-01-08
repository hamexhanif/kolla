package team5.prototype.tenant;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TenantControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mockMvc;

    @Mock
    private TenantService tenantService;

    @InjectMocks
    private TenantController tenantController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(tenantController).build();
    }

    @Test
    void createTenantReturnsEntity() throws Exception {
        Tenant request = Tenant.builder()
                .name("Tenant A")
                .subdomain("tenant-a")
                .active(true)
                .build();
        Tenant created = Tenant.builder()
                .id(1L)
                .name("Tenant A")
                .subdomain("tenant-a")
                .active(true)
                .build();

        when(tenantService.createTenant(any(Tenant.class))).thenReturn(created);

        mockMvc.perform(post("/api/tenants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.subdomain").value("tenant-a"));
    }

    @Test
    void getAllTenantsReturnsList() throws Exception {
        Tenant tenant = Tenant.builder()
                .id(2L)
                .name("Tenant B")
                .subdomain("tenant-b")
                .active(true)
                .build();

        when(tenantService.getAllTenants()).thenReturn(List.of(tenant));

        mockMvc.perform(get("/api/tenants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].name").value("Tenant B"));
    }

    @Test
    void getTenantByIdReturnsNotFoundWhenMissing() throws Exception {
        when(tenantService.getTenantById(9L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/tenants/9"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateTenantReturnsEntity() throws Exception {
        Tenant request = Tenant.builder()
                .name("Tenant C")
                .subdomain("tenant-c")
                .active(false)
                .build();
        Tenant updated = Tenant.builder()
                .id(5L)
                .name("Tenant C")
                .subdomain("tenant-c")
                .active(false)
                .build();

        when(tenantService.updateTenant(any(Long.class), any(Tenant.class))).thenReturn(updated);

        mockMvc.perform(put("/api/tenants/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void deleteTenantReturnsNoContent() throws Exception {
        doNothing().when(tenantService).deleteTenant(4L);

        mockMvc.perform(delete("/api/tenants/4"))
                .andExpect(status().isNoContent());
    }
}
