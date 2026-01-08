package team5.prototype.security;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthDto {

    // KORREKTUR: Wir ersetzen 'username' durch 'email', um zum Frontend zu passen
    private String email;

    private String password;
    private String token;

    // Konstruktor für die Antwort (bleibt gleich)
    public AuthDto(String token) {
        this.token = token;
    }

    // Standard-Konstruktor (bleibt gleich)
    public AuthDto() {
    }
}