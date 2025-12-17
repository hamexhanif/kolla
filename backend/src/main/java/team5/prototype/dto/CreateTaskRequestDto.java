package team5.prototype.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Map;

public record CreateTaskRequestDto(
        @NotNull Long workflowDefinitionId,
        @NotBlank String title,
        String description,
        @NotNull @FutureOrPresent LocalDateTime deadline,
        @NotNull Long creatorUserId,
        Map<Long, Long> stepAssignments
) {
}
