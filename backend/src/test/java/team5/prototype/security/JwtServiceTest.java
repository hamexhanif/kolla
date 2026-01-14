package team5.prototype.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService(
            "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef"
    );

    @Test
    void generatesTokenAndValidates() {
        UserDetails userDetails = User.withUsername("user@example.com")
                .password("x")
                .authorities("ROLE_USER")
                .build();

        String token = jwtService.generateToken(userDetails, List.of("ROLE_USER"));

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractEmail(token)).isEqualTo("user@example.com");
        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
    }

    @Test
    void isTokenValidReturnsFalseForOtherUser() {
        UserDetails userDetails = User.withUsername("user@example.com")
                .password("x")
                .authorities("ROLE_USER")
                .build();
        UserDetails other = User.withUsername("other@example.com")
                .password("x")
                .authorities("ROLE_USER")
                .build();

        String token = jwtService.generateToken(userDetails, List.of("ROLE_USER"));

        assertThat(jwtService.isTokenValid(token, other)).isFalse();
    }
}
