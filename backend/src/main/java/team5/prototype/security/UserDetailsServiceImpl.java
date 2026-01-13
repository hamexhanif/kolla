package team5.prototype.security;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import team5.prototype.user.User;
import team5.prototype.user.UserRepository;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;
    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Benutzer nicht gefunden: " + email));

        log.debug("=== DEBUG USER LOAD ===");
        log.debug("Email: {}", email);
        log.debug("User roles from DB: {}", user.getRoles());

        var authorities = user.getRoles().stream()
                .map(role -> {
                    log.debug("  -> Role name from DB: '{}'", role.getName());
                    String authorityString = "ROLE_" + role.getName();
                    log.debug("  -> Created authority: '{}'", authorityString);
                    return new SimpleGrantedAuthority("ROLE_" + role.getName());
                })
                .collect(Collectors.toSet());

        log.debug("Final authorities list: {}", authorities);
        log.debug("=======================");

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(),
                authorities
        );
    }
}