package team5.prototype.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import team5.prototype.role.Role;
import team5.prototype.user.User;
import team5.prototype.user.UserRepository;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecretKey jwtSecretKey;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           @Value("${jwt.secret}") String jwtSecret) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtSecretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public AuthDto login(String email, String password) {
        // Finde den Benutzer in der Datenbank anhand seiner E-Mail
        return userRepository.findByEmail(email)
                .map(user -> {
                    // Benutzer gefunden. Überprüfe das Passwort mit dem korrekten Encoder.
                    if (passwordEncoder.matches(password, user.getPasswordHash())) {
                        log.info("Login erfolgreich für: {}", email);
                        String token = createToken(user);
                        return new AuthDto(token, user.getId());
                    } else {
                        log.warn("Login-Fehler: Falsches Passwort für: {}", email);
                        return null;
                    }
                })
                .orElseGet(() -> {
                    log.warn("Login-Fehler: Benutzer nicht gefunden: {}", email);
                    return null;
                });
    }

    private String createToken(User user) {
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        long expMillis = nowMillis + 1000 * 60 * 60 * 24; // 24 Stunden
        Date exp = new Date(expMillis);

        List<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        return Jwts.builder()
                .setSubject(user.getEmail()) // Wir verwenden die E-Mail als "Subject"
                .claim("roles", roleNames)
                .claim("userId", user.getId())
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(jwtSecretKey, SignatureAlgorithm.HS512)
                .compact();
    }
}
