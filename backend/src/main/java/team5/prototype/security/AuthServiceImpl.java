package team5.prototype.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);
    private final UserRepository userRepository;

    // KORREKTUR: Wir verwenden den allgemeinen Interface-Typ, nicht die spezifische Implementierung.
    private final PasswordEncoder passwordEncoder;

    private final SecretKey jwtSecretKey;

    // KORREKTUR: Wir injizieren den zentralen PasswordEncoder von Spring.
    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           @Value("${jwt.secret}") String jwtSecret) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtSecretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String login(String email, String password) {
        // 1. Finde den Benutzer in der Datenbank anhand seiner E-MAIL.
        return userRepository.findByEmail(email)
                .map(user -> {
                    // 2. Benutzer gefunden. Überprüfe das Passwort mit dem korrekten Encoder.
                    if (passwordEncoder.matches(password, user.getPasswordHash())) {
                        logger.info("Login erfolgreich für: {}", email);
                        return createToken(user);
                    } else {
                        logger.warn("Login-Fehler: Falsches Passwort für: {}", email);
                        return null;
                    }
                })
                .orElseGet(() -> {
                    logger.warn("Login-Fehler: Benutzer nicht gefunden: {}", email);
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
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(jwtSecretKey, SignatureAlgorithm.HS512)
                .compact();
    }
}