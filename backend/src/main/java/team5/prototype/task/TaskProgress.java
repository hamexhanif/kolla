package team5.prototype.task;

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
