package team5.prototype.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import team5.prototype.dto.ManualPriorityRequestDto;
import team5.prototype.dto.TaskStepDto;
import team5.prototype.service.UserService;

@RestController
@RequestMapping("/api/task-steps")
@RequiredArgsConstructor
public class TaskStepController {

    private final UserService userService;
    private final TaskDtoMapper mapper;

    @PatchMapping("/{taskStepId}/priority")
    public TaskStepDto overrideManualPriority(@PathVariable Long taskStepId,
                                              @Valid @RequestBody ManualPriorityRequestDto requestDto) {
        return mapper.toTaskStepDto(userService.overrideManualPriority(taskStepId, requestDto.manualPriority()));
    }
}
