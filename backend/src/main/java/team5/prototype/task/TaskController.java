package team5.prototype.task;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import team5.prototype.taskstep.TaskStep;
import team5.prototype.taskstep.TaskStepDto;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // --- Endpunkte ---

    @PostMapping
    public TaskDto createTask(@RequestBody TaskDto requestDto) {
        // KORREKTER AUFRUF: Übergibt das gesamte DTO-Objekt
        Task createdTask = taskService.createTaskFromDefinition(requestDto);
        return convertToDto(createdTask);
    }

    @GetMapping
    public List<TaskDto> getAllTasks() {
        return taskService.getAllTasks().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskDto> getTaskById(@PathVariable Long id) {
        return taskService.getTaskById(id)
                .map(task -> ResponseEntity.ok(convertToDto(task)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/progress")
    public ResponseEntity<TaskProgress> getTaskProgress(@PathVariable Long id) {
        TaskProgress progress = taskService.getTaskProgress(id);
        return ResponseEntity.ok(progress);
    }

    // --- Private Konvertierungs-Hilfsmethoden ---

    private TaskDto convertToDto(Task task) {
        TaskDto dto = new TaskDto();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDeadline(task.getDeadline());
        if (task.getStatus() != null) {
            dto.setStatus(task.getStatus().name());
        }

        if (task.getTaskSteps() != null) {
            List<TaskStepDto> stepDtos = task.getTaskSteps().stream()
                    .map(this::convertStepToDto)
                    .collect(Collectors.toList());
            dto.setSteps(stepDtos);
        }
        return dto;
    }

    private TaskStepDto convertStepToDto(TaskStep step) {
        TaskStepDto dto = new TaskStepDto();
        dto.setId(step.getId());
        if (step.getWorkflowStep() != null) {
            dto.setName(step.getWorkflowStep().getName());
        }
        if (step.getStatus() != null) {
            dto.setStatus(step.getStatus().name());
        }
        if (step.getAssignedUser() != null) {
            dto.setAssignedUsername(step.getAssignedUser().getUsername());
        }
        return dto;
    }
}