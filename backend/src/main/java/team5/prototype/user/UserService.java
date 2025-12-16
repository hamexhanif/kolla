package team5.prototype.user;

import java.util.List;
import java.util.Optional;

public interface UserService {

    /**
     * Erstellt einen neuen Benutzer.
     */
    User createUser(User user);

    /**
     * Ruft alle Benutzer ab.
     */
    List<User> getAllUsers();

    /**
     * Ruft einen Benutzer anhand seiner ID ab.
     */
    Optional<User> getUserById(Long userId);

    /**
     * Aktualisiert einen bestehenden Benutzer.
     */
    User updateUser(Long userId, User userDetails);

    /**
     * Löscht einen Benutzer.
     */
    void deleteUser(Long userId);
}