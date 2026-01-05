package team5.prototype.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUserRequestDto {
    private String username;
    private String email;
    private String password; // Wir nehmen das Passwort als Klartext an
    private String firstName;
    private String lastName;
    private Long tenantId;
}