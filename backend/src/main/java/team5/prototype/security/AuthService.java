package team5.prototype.security;

public interface AuthService {
    /**
     * Authentifiziert einen Benutzer und gibt bei Erfolg einen Token zurück.
     *
     * @param email    Der Benutzername.
     * @param password Das Passwort.
     * @return Ein String, der den JWT-Token repräsentiert.
     */
    AuthDto login(String email, String password);

    /**
     * Authentifiziert einen Benutzer innerhalb eines bestimmten Mandanten.
     * @param username Der Benutzername.
     * @param password Das Passwort.
     * @param tenantId Die ID des Mandanten (optional).
     * @return Ein String, der den JWT-Token repräsentiert.
     */
    String login(String username, String password, Long tenantId);

}
