package team5.prototype.dto;

import team5.prototype.entity.TaskStatus;

import java.time.LocalDateTime;
import java.util.List;

public record TaskResponseDto(
        Long id,
        String title,
        String description,
        LocalDateTime deadline,
        TaskStatus status,
        List<TaskStepDto> steps
) {
}
