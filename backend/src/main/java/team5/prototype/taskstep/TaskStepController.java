package team5.prototype.taskstep;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import team5.prototype.dto.CompleteStepRequestDto;

@RestController
@RequestMapping("/api") // Wir legen eine allgemeine Basis-URL fest
public class TaskStepController {

    private final TaskStepService taskStepService;

    public TaskStepController(TaskStepService taskStepService) {
        this.taskStepService = taskStepService;
    }

    // Die URL aus der README: POST /api/tasks/{taskId}/steps/{stepId}/complete
    @PostMapping("/tasks/{taskId}/steps/{stepId}/complete")
    public ResponseEntity<Void> completeTaskStep(@PathVariable Long taskId,
                                                 @PathVariable Long stepId,
                                                 @RequestBody CompleteStepRequestDto request) {

        // Ruft die korrekte Methode im Service auf
        taskStepService.completeTaskStep(taskId, stepId, request.getUserId());

        return ResponseEntity.ok().build();
    }
}