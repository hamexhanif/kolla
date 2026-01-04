package team5.prototype.security;
import org.springframework.security.core.authority.SimpleGrantedAuthority; import org.springframework.security.core.userdetails.*; import org.springframework.stereotype.Service; import team5.prototype.user.User; import team5.prototype.user.UserRepository; import java.util.stream.Collectors;
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;
    public UserDetailsServiceImpl(UserRepository userRepository) { this.userRepository = userRepository; }
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("Benutzer nicht gefunden: " + username));
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPasswordHash(),
                user.getRoles().stream().map(role -> new SimpleGrantedAuthority(role.getName())).collect(Collectors.toSet()));
    }
}