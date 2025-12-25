package team5.prototype.dto;

import team5.prototype.taskstep.Priority;

public record ManagerTaskRowDto(
        Long taskId,
        String title,
        Priority priority,
        int completedSteps,
        int totalSteps
) {
}
