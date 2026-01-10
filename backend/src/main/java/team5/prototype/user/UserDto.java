package team5.prototype.user;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

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
    private String firstName;
    private String lastName;
    private List<String> roles;
    private long assignedTasks;
    private long completedTasks;
    private long inProgressTasks;
}