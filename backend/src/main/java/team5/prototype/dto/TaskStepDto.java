package team5.prototype.dto;

import team5.prototype.entity.Priority;
import team5.prototype.entity.TaskStepStatus;

import java.time.LocalDateTime;

public record TaskStepDto(
        Long id,
        Long taskId,
        String taskTitle,
        Long assignedUserId,
        String workflowStepName,
        Integer workflowStepOrder,
        TaskStepStatus status,
        Priority priority,
        Integer manualPriority,
        LocalDateTime assignedAt,
        LocalDateTime completedAt
) {
}
