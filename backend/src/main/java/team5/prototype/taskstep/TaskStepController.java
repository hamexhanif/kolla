package team5.prototype.taskstep;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import team5.prototype.dto.CompleteStepRequestDto; // Stelle sicher, dass der Import-Pfad korrekt ist

@RestController
@RequestMapping("/api/task-steps") // Eine Basis-URL für alle Step-bezogenen Operationen
public class TaskStepController {

    private final TaskStepService taskStepService;

    public TaskStepController(TaskStepService taskStepService) {
        this.taskStepService = taskStepService;
    }

    // Endpunkt zum Abschließen eines Schrittes
    @PostMapping("/{stepId}/complete")
    public ResponseEntity<Void> completeTaskStep(@PathVariable Long stepId,
                                                 @RequestBody CompleteStepRequestDto request) {

        // Die Logik wird an den Service delegiert
        taskStepService.completeTaskStep(request.getTaskId(), stepId, request.getUserId());

        return ResponseEntity.ok().build();
    }
}