package team5.prototype.tenant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import team5.prototype.security.UserPrincipal;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest
class TenantSecurityTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void getAllTenantsRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/tenants"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllTenantsAllowsAuthenticatedUser() throws Exception {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                new UserPrincipal("user", 1L), null, java.util.List.of());

        mockMvc.perform(get("/api/tenants").with(authentication(auth)))
                .andExpect(status().isOk());
    }
}
