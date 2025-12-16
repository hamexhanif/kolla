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

    // --- Konvertierungslogik ---
    private TaskDto convertToDto(Task task) {
        TaskDto dto = new TaskDto();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDeadline(task.getDeadline());
        dto.setStatus(task.getStatus().name());

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
        // Annahme: Die Namen kommen aus der WorkflowStep-Vorlage
        // if (step.getWorkflowStep() != null) {
        //     dto.setName(step.getWorkflowStep().getName());
        // }
        dto.setStatus(step.getStatus().name());
        if (step.getAssignedUser() != null) {
            dto.setAssignedUsername(step.getAssignedUser().getUsername());
        }
        return dto;
    }


    // --- Endpunkte ---
    @PostMapping
    public TaskDto createTask(@RequestBody TaskDto requestDto) {
        Task createdTask = taskService.createTaskFromDefinition(requestDto.getDefinitionId(), requestDto.getTitle());
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
}