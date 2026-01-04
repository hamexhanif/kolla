package team5.prototype.dto;

import team5.prototype.taskstep.Priority;
import team5.prototype.taskstep.TaskStepStatus;

import java.time.LocalDateTime;

public record TaskDetailsStepDto(
        Long stepId,
        String stepName,
        TaskStepStatus status,
        String assigneeName,
        LocalDateTime dueDate,
        Priority priority
) {
}
