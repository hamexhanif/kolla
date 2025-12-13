package team5.prototype.dto;

import team5.prototype.entity.TaskStatus;

import java.time.LocalDateTime;

public record TaskProgressDto(
        Long taskId,
        String title,
        LocalDateTime deadline,
        int totalSteps,
        int completedSteps,
        TaskStatus status
) {
}
