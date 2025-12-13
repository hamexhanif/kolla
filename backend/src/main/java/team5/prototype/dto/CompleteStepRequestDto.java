package team5.prototype.dto;

import jakarta.validation.constraints.NotNull;

public record CompleteStepRequestDto(
        @NotNull Long userId
) {
}
