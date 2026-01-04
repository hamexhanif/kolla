package team5.prototype.taskstep;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import team5.prototype.dto.ActorDashboardItemDto;
import team5.prototype.dto.CompleteStepRequestDto;
import team5.prototype.dto.ManualPriorityRequestDto;

import java.util.List;

@RestController
@RequestMapping("/api/task-steps")
public class TaskStepController {

    private final TaskStepService taskStepService;

    public TaskStepController(TaskStepService taskStepService) {
        this.taskStepService = taskStepService;
    }

    @PostMapping("/{stepId}/complete")
    public ResponseEntity<Void> completeTaskStep(@PathVariable Long stepId,
                                                 @RequestBody CompleteStepRequestDto request) {
        taskStepService.completeTaskStep(request.getTaskId(), stepId, request.getUserId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{stepId}/set-priority")
    public ResponseEntity<TaskStepDto> setManualPriority(@PathVariable Long stepId,
                                                         @RequestBody ManualPriorityRequestDto request) {
        TaskStepDto responseDto = taskStepService.setManualPriorityAndConvertToDto(stepId, request.manualPriority());
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/actor-dashboard")
    public ResponseEntity<List<ActorDashboardItemDto>> getActorDashboardItems(
            @RequestParam Long userId,
            @RequestParam(required = false) TaskStepStatus status,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) String query) {
        List<ActorDashboardItemDto> items = taskStepService.getActorDashboardItems(userId, status, priority, query);
        return ResponseEntity.ok(items);
    }
}
