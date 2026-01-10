package team5.prototype.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import team5.prototype.role.Role;
import team5.prototype.user.User;
import team5.prototype.user.UserRepository;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    private UserDetailsServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UserDetailsServiceImpl(userRepository);
    }

    @Test
    void loadUserByUsernameReturnsUserDetails() {
        Role admin = Role.builder().name("ADMIN").build();
        Role reviewer = Role.builder().name("REVIEWER").build();
        User user = User.builder()
                .email("user@example.com")
                .passwordHash("hash")
                .roles(Set.of(admin, reviewer))
                .build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        UserDetails details = service.loadUserByUsername("user@example.com");

        assertThat(details.getUsername()).isEqualTo("user@example.com");
        assertThat(details.getPassword()).isEqualTo("hash");
        assertThat(details.getAuthorities())
                .extracting(Object::toString)
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_REVIEWER");
    }

    @Test
    void loadUserByUsernameHandlesEmptyRoles() {
        User user = User.builder()
                .email("user@example.com")
                .passwordHash("hash")
                .build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        UserDetails details = service.loadUserByUsername("user@example.com");

        assertThat(details.getAuthorities()).isEmpty();
    }

    @Test
    void loadUserByUsernameThrowsWhenMissing() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("missing@example.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
