package team5.prototype.task;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import team5.prototype.dto.ManagerDashboardDto;
import team5.prototype.dto.TaskDetailsDto;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public TaskDto createTask(@RequestBody TaskDto requestDto) {
        Task createdTask = taskService.createTaskFromDefinition(requestDto);
        // Die Konvertierung passiert noch hier, aber das ist OK weil createTaskFromDefinition @Transactional ist
        return convertToDto(createdTask);
    }

    @GetMapping
    public List<TaskDto> getAllTasks() {
        // NEUE Methode verwenden - Konvertierung passiert im Service
        return taskService.getAllTasksAsDto();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskDto> getTaskById(@PathVariable Long id) {
        // NEUE Methode verwenden - Konvertierung passiert im Service
        return taskService.getTaskByIdAsDto(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/progress")
    public ResponseEntity<TaskProgress> getTaskProgress(@PathVariable Long id) {
        TaskProgress progress = taskService.getTaskProgress(id);
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<TaskDetailsDto> getTaskDetails(@PathVariable Long id) {
        TaskDetailsDto details = taskService.getTaskDetails(id);
        return ResponseEntity.ok(details);
    }

    @GetMapping("/manager-dashboard")
    public ResponseEntity<ManagerDashboardDto> getManagerDashboard() {
        return ResponseEntity.ok(taskService.getManagerDashboard());
    }

    // Diese Methode nur noch fuer createTask verwendet
    private TaskDto convertToDto(Task task) {
        TaskDto dto = new TaskDto();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDeadline(task.getDeadline());
        if (task.getStatus() != null) {
            dto.setStatus(task.getStatus().name());
        }
        return dto;
    }
}
