package team5.prototype.dto;

import team5.prototype.task.TaskStatus;
import team5.prototype.taskstep.Priority;
import team5.prototype.taskstep.TaskStepStatus;

import java.time.LocalDateTime;

public record ActorDashboardItemDto(
        Long taskId,
        String taskTitle,
        LocalDateTime taskDeadline,
        TaskStatus taskStatus,
        Long stepId,
        String stepName,
        Integer stepOrder,
        TaskStepStatus stepStatus,
        Priority priority,
        LocalDateTime assignedAt
) {
}
