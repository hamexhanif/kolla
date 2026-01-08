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
    public ResponseEntity<AuthDto> login(@RequestBody AuthDto authDto) { // Verwendet jetzt AuthDto
        String identifier = authDto.getEmail();
        if (identifier == null || identifier.isBlank()) {
            identifier = authDto.getUsername();
        }
        String token = authService.login(identifier, authDto.getPassword(), authDto.getTenantId());
        if (token != null) {
            return ResponseEntity.ok(new AuthDto(token));
        } else {
            return ResponseEntity.status(401).build();
        }
    }
}
