package team5.prototype.service;

public interface AuthService {
    /**
     * Authentifiziert einen Benutzer und gibt bei Erfolg einen Token zurück.
     * @param username Der Benutzername.
     * @param password Das Passwort.
     * @return Ein String, der den JWT-Token repräsentiert.
     */
    String login(String username, String password);
}