package team5.prototype.security;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import team5.prototype.user.User;
import team5.prototype.user.UserRepository;
import java.util.stream.Collectors;

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

        System.out.println("=== DEBUG USER LOAD ===");
        System.out.println("Email: " + email);
        System.out.println("User roles from DB: " + user.getRoles());

        var authorities = user.getRoles().stream()
                .map(role -> {
                    System.out.println("  -> Role name from DB: '" + role.getName() + "'");
                    String authorityString = "ROLE_" + role.getName();
                    System.out.println("  -> Created authority: '" + authorityString + "'");
                    return new SimpleGrantedAuthority("ROLE_" + role.getName());
                })
                .collect(Collectors.toSet());

        System.out.println("Final authorities list: " + authorities);
        System.out.println("=======================");

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(),
                authorities
        );
    }
}