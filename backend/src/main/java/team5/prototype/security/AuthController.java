package team5.prototype.security;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import team5.prototype.role.Role;
import team5.prototype.user.User;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AuthService authService; // Wir behalten ihn für die Logik

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService, AuthService authService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthDto> login(@RequestBody AuthDto authDto) {
        // ===================================================================
        // HIER IST DIE ENTSCHEIDENDE ÄNDERUNG
        // Wir übergeben die Authentifizierung an den AuthenticationManager von Spring
        // ===================================================================
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authDto.getUsername(), authDto.getPassword())
        );

        // Wenn die Zeile oben keinen Fehler wirft, war die Authentifizierung erfolgreich.
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Wir holen die Details des authentifizierten Benutzers
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // Wir holen die Rollen aus den Authorities
        List<String> roles = userDetails.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .collect(Collectors.toList());

        // Wir generieren den Token
        String token = jwtService.generateToken(userDetails, roles);

        // Wir geben den Token zurück
        return ResponseEntity.ok(new AuthDto(token));
    }
}