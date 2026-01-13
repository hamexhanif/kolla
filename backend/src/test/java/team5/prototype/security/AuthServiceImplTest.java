package team5.prototype.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import team5.prototype.role.Role;
import team5.prototype.user.User;
import team5.prototype.user.UserRepository;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthServiceImpl authService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(
                userRepository,
                passwordEncoder,
                "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef"
        );
    }

    @Test
    void loginReturnsTokenWhenCredentialsMatch() {
        Role role = Role.builder().id(1L).name("ADMIN").build();
        User user = User.builder()
                .id(10L)
                .email("a@b.com")
                .passwordHash("hash")
                .roles(Set.of(role))
                .build();

        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("p", "hash")).thenReturn(true);

        AuthDto result = authService.login("a@b.com", "p");

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(10L);
        assertThat(result.getToken()).isNotBlank();
    }

    @Test
    void loginReturnsNullWhenPasswordInvalid() {
        User user = User.builder().id(10L).email("a@b.com").passwordHash("hash").build();

        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("p", "hash")).thenReturn(false);

        AuthDto result = authService.login("a@b.com", "p");

        assertThat(result).isNull();
    }

    @Test
    void loginReturnsNullWhenUserMissing() {
        when(userRepository.findByEmail("missing@b.com")).thenReturn(Optional.empty());

        AuthDto result = authService.login("missing@b.com", "p");

        assertThat(result).isNull();
    }
}
