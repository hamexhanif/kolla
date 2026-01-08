package team5.prototype.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import team5.prototype.user.User;
import team5.prototype.user.UserRepository;
import team5.prototype.role.Role;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final SecretKey jwtSecretKey;

    public AuthServiceImpl(UserRepository userRepository, @Value("${jwt.secret}") String jwtSecret) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        // Erstellt einen sicheren Schlüssel aus dem String in application.properties
        this.jwtSecretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String login(String username, String password) {
        return login(username, password, null);
    }

    @Override
    public String login(String username, String password, Long tenantId) {
        // 1. Finde den Benutzer in der Datenbank anhand seines Benutzernamens.
        return resolveUser(username, tenantId)
                .map(user -> {
                    // 2. Benutzer gefunden. Überprüfe jetzt das Passwort.
                    // passwordEncoder.matches() vergleicht das eingegebene Klartext-Passwort
                    // mit dem sicheren Hash, der in der Datenbank gespeichert ist.
                    if (passwordEncoder.matches(password, user.getPasswordHash())) {
                        // 3. Passwort ist korrekt. Erstelle und gib einen JWT-Token zurück.
                        System.out.println("Login erfolgreich für: " + username);
                        return createToken(user);
                    } else {
                        // 4. Passwort ist falsch.
                        System.out.println("Login-Fehler: Falsches Passwort für: " + username);
                        return null;
                    }
                })
                .orElseGet(() -> {
                    // 5. Benutzer wurde gar nicht in der Datenbank gefunden.
                    System.out.println("Login-Fehler: Benutzer nicht gefunden: " + username);
                    return null;
                });
    }

    private java.util.Optional<User> resolveUser(String username, Long tenantId) {
        if (tenantId != null) {
            return userRepository.findByUsernameAndTenant_Id(username, tenantId);
        }
        return userRepository.findByUsername(username);
    }

    private String createToken(User user) {
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        long expMillis = nowMillis + 1000 * 60 * 60 * 24; // 24 Stunden
        Date exp = new Date(expMillis);

        // Konvertiert das Set der Rollen-Objekte in eine Liste von Rollen-Namen (Strings)
        List<String> roleNames = user.getRoles().stream() // Ruft getRoles() (Plural) auf
                .map(Role::getName)
                .collect(Collectors.toList());

        var builder = Jwts.builder()
                .setSubject(user.getUsername())
                .claim("roles", roleNames) // Fügt die Liste der Rollen-Namen zum Token hinzu
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(jwtSecretKey, SignatureAlgorithm.HS512);

        if (user.getTenant() != null) {
            builder.claim("tenantId", user.getTenant().getId());
        }

        return builder.compact();
    }
}
