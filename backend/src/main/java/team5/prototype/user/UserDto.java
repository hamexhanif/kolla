package team5.prototype.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDto {

    /**
     * Die eindeutige ID des Benutzers.
     */
    private Long id;

    /**
     * Der Benutzername.
     */
    private String username;

    /**
     * Die E-Mail-Adresse des Benutzers.
     */
    private String email;

    // Wichtig: Diese Klasse enthält absichtlich KEIN Passwort oder andere sensible Daten.
}