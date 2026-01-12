package team5.prototype.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import team5.prototype.dto.ActorDashboardItemDto;
import team5.prototype.dto.CreateUserRequestDto;
import team5.prototype.taskstep.TaskStep;
import team5.prototype.taskstep.TaskStepService;
import team5.prototype.taskstep.TaskStepStatus;
import team5.prototype.role.Role;

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
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(user -> ResponseEntity.ok(convertToDto(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public UserDto createUser(@RequestBody CreateUserRequestDto requestDto) {
        User createdUser = userService.createUser(requestDto);
        return convertToDto(createdUser);
    }

    @PutMapping("/{id}")
    public UserDto updateUser(@PathVariable Long id, @RequestBody UpdateUserRequestDto requestDto) {
        User updatedUser = userService.updateUser(id, requestDto);
        return convertToDto(updatedUser);
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
    private UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());

        if (user.getRoles() != null) {
            dto.setRoles(user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toList()));
        }
        List<TaskStep> allUserSteps = taskStepService.getAllTaskStepsByUserId(user.getId());

        long assigned = allUserSteps.stream().filter(s -> s.getStatus() == TaskStepStatus.WAITING).count();
        long completed = allUserSteps.stream().filter(s -> s.getStatus() == TaskStepStatus.COMPLETED).count();
        long inProgress = allUserSteps.stream().filter(s -> s.getStatus() == TaskStepStatus.ASSIGNED).count();

        dto.setAssignedTasks(assigned);
        dto.setCompletedTasks(completed);
        dto.setInProgressTasks(inProgress);

        return dto;
    }
}