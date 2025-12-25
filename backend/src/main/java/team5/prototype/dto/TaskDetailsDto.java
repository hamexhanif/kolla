package team5.prototype.dto;

import team5.prototype.taskstep.Priority;

import java.time.LocalDateTime;
import java.util.List;

public record TaskDetailsDto(
        Long taskId,
        String title,
        Priority priority,
        LocalDateTime deadline,
        int completedSteps,
        int totalSteps,
        List<TaskDetailsStepDto> steps
) {
}
