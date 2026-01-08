package team5.prototype.security;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL) // Wichtig: Sendet nur Felder, die nicht null sind
public class AuthDto {

    // Felder für die Login-Anfrage (Request)
    private String username;
    private String password;
    private Long tenantId;

    // Feld für die Login-Antwort (Response)
    private String token;

    // Konstruktor für die Antwort
    public AuthDto(String token) {
        this.token = token;
    }

    // Standard-Konstruktor, den Jackson (die JSON-Bibliothek) benötigt
    public AuthDto() {
    }
}
