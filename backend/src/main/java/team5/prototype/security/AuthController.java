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
        // Call service method that handles all business logic including DB lookup
        AuthDto response = authService.login(authDto.getEmail(), authDto.getPassword());

        if (response != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(401).build();
        }
    }
}