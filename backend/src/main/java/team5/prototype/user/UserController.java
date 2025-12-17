package team5.prototype.user;

// --- HIER KOMMEN DIE WICHTIGEN IMPORTS ---
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Hilfsmethode, um ein User-Objekt in ein UserDto umzuwandeln
    private UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        //Annahme: UserDto hat die entsprechenden Setter-Methoden
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        return dto;
    }

    // ENDPUNKT: GET /api/users - Ruft alle Benutzer ab
    @GetMapping
    public List<UserDto> getAllUsers() {
        return userService.getAllUsers().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // ENDPUNKT: GET /api/users/{id} - Ruft einen Benutzer ab
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(user -> ResponseEntity.ok(convertToDto(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    // ENDPUNKT: POST /api/users - Erstellt einen neuen Benutzer
    @PostMapping
    public UserDto createUser(@RequestBody User user) {
        User createdUser = userService.createUser(user);
        return convertToDto(createdUser);
    }

    // ENDPUNKT: PUT /api/users/{id} - Aktualisiert einen Benutzer
    @PutMapping("/{id}")
    public UserDto updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        User updatedUser = userService.updateUser(id, userDetails);
        return convertToDto(updatedUser);
    }

    // ENDPUNKT: DELETE /api/users/{id} - Löscht einen Benutzer
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}