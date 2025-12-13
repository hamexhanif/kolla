package team5.prototype.service;

import team5.prototype.entity.TaskStatus;

import java.time.LocalDateTime;

/**
 * Lightweight snapshot used by the workflow manager to monitor an individual task.
 */
public record TaskProgress(
        Long taskId,
        String title,
        LocalDateTime deadline,
        int totalSteps,
        int completedSteps,
        TaskStatus status
) {
}
