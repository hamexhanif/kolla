package team5.prototype.security;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthDto> login(@RequestBody AuthDto authDto) {
        // ===================================================================
        // KORREKTUR: Wir verwenden jetzt getEmail() statt getUsername()
        // ===================================================================
        String token = authService.login(authDto.getEmail(), authDto.getPassword());

        if (token != null) {
            return ResponseEntity.ok(new AuthDto(token));
        } else {
            return ResponseEntity.status(401).build();
        }
    }
}