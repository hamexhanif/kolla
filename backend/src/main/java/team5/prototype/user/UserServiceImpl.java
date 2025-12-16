package team5.prototype.user;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    // Konstruktor-Injection: Spring liefert uns automatisch das UserRepository
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // --- HIER BEGINNT DIE IMPLEMENTIERUNG DER TO-DO-LISTE ---

    /**
     * Erstellt einen neuen Benutzer und speichert ihn in der Datenbank.
     * @param user Das zu erstellende User-Objekt.
     * @return Der gespeicherte User (inklusive der generierten ID).
     */
    public User createUser(User user) {
        // Später könnten hier Validierungen hinzugefügt werden (z.B. Passwortstärke prüfen)
        return userRepository.save(user);
    }

    /**
     * Ruft alle Benutzer aus der Datenbank ab.
     * @return Eine Liste aller User-Objekte.
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Ruft einen einzelnen Benutzer anhand seiner ID ab.
     * @param userId Die ID des zu suchenden Benutzers.
     * @return Ein Optional, das den Benutzer enthält, falls er gefunden wurde.
     */
    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }

    /**
     * Aktualisiert die Daten eines bestehenden Benutzers.
     * @param userId Die ID des zu aktualisierenden Benutzers.
     * @param userDetails Ein User-Objekt mit den neuen Daten.
     * @return Der aktualisierte und gespeicherte User.
     */
    public User updateUser(Long userId, User userDetails) {
        // Finde den existierenden Benutzer oder wirf eine Ausnahme
        return userRepository.findById(userId)
                .map(existingUser -> {
                    // Aktualisiere nur die Felder, die geändert werden dürfen
                    existingUser.setUsername(userDetails.getUsername());
                    existingUser.setEmail(userDetails.getEmail());
                    // Das Passwort sollte über eine separate Methode geändert werden
                    // existingUser.setRoles(userDetails.getRoles()); // Falls die Rollen auch änderbar sein sollen

                    return userRepository.save(existingUser);
                })
                .orElseThrow(() -> new RuntimeException("Benutzer mit ID " + userId + " nicht gefunden!"));
    }

    /**
     * Löscht einen Benutzer anhand seiner ID.
     * @param userId Die ID des zu löschenden Benutzers.
     */
    public void deleteUser(Long userId) {
        // Prüfen, ob der Benutzer existiert, bevor versucht wird, ihn zu löschen
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("Benutzer mit ID " + userId + " nicht gefunden!");
        }
        userRepository.deleteById(userId);
    }
}