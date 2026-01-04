// VEREINFACHTE AuthServiceImpl.java

package team5.prototype.security;

import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    // Der Service ist jetzt viel einfacher. Die komplexe Logik
    // liegt im AppAuthenticationProvider und im AuthController.

    @Override
    public String login(String username, String password) {
        // Diese Methode wird im neuen Flow nicht mehr direkt aufgerufen,
        // aber wir lassen sie für die Schnittstellen-Konformität drin.
        // In einem echten Refactoring könnte man sie entfernen.
        throw new UnsupportedOperationException("Diese Methode sollte nicht direkt aufgerufen werden. Der AuthenticationManager wird verwendet.");
    }
}