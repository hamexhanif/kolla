package team5.prototype.task;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
        return taskService.createTaskFromDefinition(requestDto);
    }

    @GetMapping
    public List<TaskDto> getAllTasks() {
        return taskService.getAllTasksAsDto();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskDto> getTaskById(@PathVariable Long id) {
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
}
