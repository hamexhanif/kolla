package team5.prototype.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";

    private JwtService jwtService;
    private SecretKey secretKey;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET);
        secretKey = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void generateTokenIncludesRolesAndSubject() {
        UserDetails user = User.withUsername("user@example.com")
                .password("pw")
                .authorities("ROLE_USER")
                .build();

        String token = jwtService.generateToken(user, List.of("ADMIN"));

        assertThat(jwtService.extractEmail(token)).isEqualTo("user@example.com");
        Claims claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
        @SuppressWarnings("unchecked")
        List<String> roles = claims.get("roles", List.class);
        assertThat(roles).containsExactly("ADMIN");
    }

    @Test
    void isTokenValidReturnsFalseForDifferentUser() {
        UserDetails user = User.withUsername("user@example.com")
                .password("pw")
                .authorities("ROLE_USER")
                .build();
        UserDetails other = User.withUsername("other@example.com")
                .password("pw")
                .authorities("ROLE_USER")
                .build();

        String token = jwtService.generateToken(user, List.of("ADMIN"));

        assertThat(jwtService.isTokenValid(token, other)).isFalse();
    }

    @Test
    void isTokenValidReturnsTrueForMatchingUser() {
        UserDetails user = User.withUsername("user@example.com")
                .password("pw")
                .authorities("ROLE_USER")
                .build();

        String token = jwtService.generateToken(user, List.of("ADMIN"));

        assertThat(jwtService.isTokenValid(token, user)).isTrue();
    }

    @Test
    void isTokenValidReturnsFalseWhenExpired() {
        UserDetails user = User.withUsername("user@example.com")
                .password("pw")
                .authorities("ROLE_USER")
                .build();

        long now = System.currentTimeMillis();
        String token = Jwts.builder()
                .setSubject("user@example.com")
                .setIssuedAt(new Date(now - 10_000))
                .setExpiration(new Date(now - 5_000))
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();

        assertThat(jwtService.isTokenValid(token, user)).isFalse();
    }
}
