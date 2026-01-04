package team5.prototype.taskstep;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import team5.prototype.dto.CompleteStepRequestDto;
import team5.prototype.dto.ManualPriorityRequestDto;

@RestController
@RequestMapping("/api/task-steps")
public class TaskStepController {

    private final TaskStepService taskStepService;

    public TaskStepController(TaskStepService taskStepService) {
        this.taskStepService = taskStepService;
    }

    // Endpunkt zum Abschliessen eines Schrittes
    @PostMapping("/{stepId}/complete")
    public ResponseEntity<Void> completeTaskStep(@PathVariable Long stepId,
                                                 @RequestBody CompleteStepRequestDto request) {

        // Die Logik wird an den Service delegiert
        taskStepService.completeTaskStep(request.getTaskId(), stepId, request.getUserId());

        return ResponseEntity.ok().build();
    }

    // Neuer Endpunkt fuer die manuelle Priorisierung
    @PostMapping("/{stepId}/set-priority")
    public ResponseEntity<TaskStepDto> setManualPriority(@PathVariable Long stepId,
                                                         @RequestBody ManualPriorityRequestDto request) {

        // Ruft die Service-Methode auf, die direkt das DTO zurueckgibt
        TaskStepDto responseDto = taskStepService.setManualPriorityAndConvertToDto(stepId, request.manualPriority());

        return ResponseEntity.ok(responseDto);
    }
}
