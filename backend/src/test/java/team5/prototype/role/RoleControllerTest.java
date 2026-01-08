package team5.prototype.role;

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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RoleControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mockMvc;

    @Mock
    private RoleService roleService;

    @InjectMocks
    private RoleController roleController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(roleController).build();
    }

    @Test
    void createRoleReturnsEntity() throws Exception {
        Role request = new Role();
        request.setName("ADMIN");
        request.setDescription("Admin role");

        Role created = new Role();
        created.setId(7L);
        created.setName("ADMIN");
        created.setDescription("Admin role");

        when(roleService.createRole(any(Role.class))).thenReturn(created);

        mockMvc.perform(post("/api/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.name").value("ADMIN"));
    }

    @Test
    void getAllRolesReturnsList() throws Exception {
        Role role = new Role();
        role.setId(4L);
        role.setName("REVIEWER");

        when(roleService.getAllRoles()).thenReturn(List.of(role));

        mockMvc.perform(get("/api/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(4))
                .andExpect(jsonPath("$[0].name").value("REVIEWER"));
    }

    @Test
    void deleteRoleReturnsNoContent() throws Exception {
        doNothing().when(roleService).deleteRole(4L);

        mockMvc.perform(delete("/api/roles/4"))
                .andExpect(status().isNoContent());
    }
}
