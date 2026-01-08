package team5.prototype.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import team5.prototype.role.Role;
import team5.prototype.tenant.Tenant;
import team5.prototype.user.User;
import team5.prototype.user.UserRepository;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class AuthServiceImplTest {

    private static final String SECRET = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";

    @Test
    void loginReturnsTokenWhenPasswordMatches() {
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        AuthServiceImpl authService = new AuthServiceImpl(userRepository, SECRET);
        String hashed = new BCryptPasswordEncoder().encode("pass");

        Role role = Role.builder().name("ADMIN").build();
        User user = User.builder()
                .username("user")
                .passwordHash(hashed)
                .roles(Set.of(role))
                .build();

        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));

        String token = authService.login("user", "pass");

        assertThat(token).isNotNull();
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        assertThat(claims.getSubject()).isEqualTo("user");
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) claims.get("roles");
        assertThat(roles).containsExactly("ADMIN");
    }

    @Test
    void loginReturnsNullWhenPasswordDoesNotMatch() {
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        AuthServiceImpl authService = new AuthServiceImpl(userRepository, SECRET);
        String hashed = new BCryptPasswordEncoder().encode("pass");

        User user = User.builder()
                .username("user")
                .passwordHash(hashed)
                .build();

        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));

        String token = authService.login("user", "wrong");

        assertThat(token).isNull();
    }

    @Test
    void loginReturnsNullWhenUserMissing() {
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        AuthServiceImpl authService = new AuthServiceImpl(userRepository, SECRET);

        when(userRepository.findByUsername("user")).thenReturn(Optional.empty());

        String token = authService.login("user", "pass");

        assertThat(token).isNull();
    }

    @Test
    void loginUsesTenantWhenProvided() {
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        AuthServiceImpl authService = new AuthServiceImpl(userRepository, SECRET);
        String hashed = new BCryptPasswordEncoder().encode("pass");

        Tenant tenant = Tenant.builder().id(9L).name("t9").build();
        User user = User.builder()
                .username("user")
                .passwordHash(hashed)
                .tenant(tenant)
                .build();

        when(userRepository.findByUsernameAndTenant_Id("user", 9L)).thenReturn(Optional.of(user));

        String token = authService.login("user", "pass", 9L);

        assertThat(token).isNotNull();
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        assertThat(claims.get("tenantId", Number.class).longValue()).isEqualTo(9L);
    }
}
