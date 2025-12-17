package team5.prototype.security;

import org.springframework.stereotype.Service;
import team5.prototype.user.UserRepository;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    public AuthServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public String login(String username, String password) {
        // TODO: Später die Logik für Passwort-Überprüfung (mit z.B. BCrypt)
        // und JWT-Erstellung implementieren.

        // Einfache Dummy-Logik für jetzt:
        return userRepository.findByUsername(username)
                .map(user -> {
                    // Dummy-Passwort-Check
                    if (password.equals("password")) { // Passwort-Check hier anpassen
                        System.out.println("Login erfolgreich für: " + username);
                        return "dummy-jwt-token-for-" + username; // Gibt einen Dummy-Token zurück
                    }
                    System.out.println("Falsches Passwort für: " + username);
                    return null; // Gibt null zurück bei falschem Passwort
                })
                .orElseGet(() -> {
                    System.out.println("Benutzer nicht gefunden: " + username);
                    return null; // Gibt null zurück, wenn Benutzer nicht existiert
                });
    }
}