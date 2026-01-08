package team5.prototype.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import team5.prototype.dto.ActorDashboardItemDto;
import team5.prototype.dto.CreateUserRequestDto;
import team5.prototype.taskstep.TaskStepService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final TaskStepService taskStepService; // Abhängigkeit ist korrekt

    // Konstruktor ist jetzt sauber
    public UserController(UserService userService, TaskStepService taskStepService) {
        System.out.println(">>> USER CONTROLLER WURDE ERSTELLT!");
        this.userService = userService;
        this.taskStepService = taskStepService;
    }

    // --- Endpunkte für die User-Verwaltung (CRUD) ---

    @GetMapping
    public List<UserDto> getAllUsers() {
        return userService.getAllUsers().stream()
                .map(this::convertToUserDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(user -> ResponseEntity.ok(convertToUserDto(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public UserDto createUser(@RequestBody CreateUserRequestDto requestDto) {
        User createdUser = userService.createUser(requestDto);
        return convertToUserDto(createdUser);
    }

    @PutMapping("/{id}")
    public UserDto updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        // HINWEIS: Hier sollte später auch ein DTO verwendet werden
        User updatedUser = userService.updateUser(id, userDetails);
        return convertToUserDto(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // ===================================================================
    // DER EINE, KORREKTE ENDPUNKT FÜR DAS AKTEUR-DASHBOARD ("My Tasks")
    // ===================================================================
    @GetMapping("/dashboard-tasks/{userId}")
    public List<ActorDashboardItemDto> getMyTasks(@PathVariable Long userId) {
        return taskStepService.getActorDashboardItems(userId);
    }


    // --- Private Konvertierungs-Hilfsmethode ---
    private UserDto convertToUserDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        return dto;
    }
}