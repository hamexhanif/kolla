package team5.prototype.task;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import team5.prototype.taskstep.TaskStepDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskDto {

    // --- Felder für ANTWORTEN (GET) ---
    private Long id;
    private String title;
    private String description;
    private LocalDateTime deadline;
    private String status;
    private List<TaskStepDto> steps;

    // --- Felder für ANFRAGEN (POST zum Erstellen) ---
    private Long workflowDefinitionId;
    private Long creatorUserId;
    private Map<Long, Long> stepAssignments;

    // Leerer Konstruktor wird von JSON-Bibliothek benötigt
    public TaskDto() {}

    public void setCreatedById(Long id) {
    }
}