package team5.prototype.security;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthDto> login(@RequestBody AuthDto authDto) { // Verwendet jetzt AuthDto
        String token = authService.login(authDto.getUsername(), authDto.getPassword(), authDto.getTenantId());
        if (token != null) {
            // Erstellt ein neues AuthDto, das nur den Token enthält
            return ResponseEntity.ok(new AuthDto(token));
        } else {
            return ResponseEntity.status(401).build(); // Unauthorized
        }
    }
}
