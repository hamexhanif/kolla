package team5.prototype.task;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import team5.prototype.taskstep.TaskStepDto; // Angepasster Name

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL) // Sendet nur Felder, die nicht null sind
public class TaskDto {

    // Felder für die Antwort (Response)
    private Long id;
    private String title;
    private LocalDateTime deadline;
    private String status;
    private List<TaskStepDto> steps;

    // Felder für die Anfrage (Request)
    private Long definitionId;

    // Leerer Konstruktor für JSON-Verarbeitung
    public TaskDto() {}
}