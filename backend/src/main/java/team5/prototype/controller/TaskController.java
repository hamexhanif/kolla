package team5.prototype.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import team5.prototype.dto.CompleteStepRequestDto;
import team5.prototype.dto.CreateTaskRequestDto;
import team5.prototype.dto.TaskProgressDto;
import team5.prototype.dto.TaskResponseDto;
import team5.prototype.service.TaskCreationRequest;
import team5.prototype.service.TaskProgress;
import team5.prototype.service.TaskService;

import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final TaskDtoMapper mapper;

    @PostMapping
    public ResponseEntity<TaskResponseDto> createTask(@Valid @RequestBody CreateTaskRequestDto requestDto) {
        TaskCreationRequest request = new TaskCreationRequest(
                requestDto.workflowDefinitionId(),
                requestDto.title(),
                requestDto.description(),
                requestDto.deadline(),
                requestDto.creatorUserId(),
                requestDto.stepAssignments() == null ? Map.of() : requestDto.stepAssignments()
        );
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(mapper.toTaskResponse(taskService.createTaskFromDefinition(request)));
    }

    @PostMapping("/{taskId}/steps/{stepId}/complete")
    public ResponseEntity<Void> completeStep(@PathVariable Long taskId,
                                             @PathVariable Long stepId,
                                             @Valid @RequestBody CompleteStepRequestDto requestDto) {
        taskService.completeStep(taskId, stepId, requestDto.userId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{taskId}/progress")
    public TaskProgressDto getProgress(@PathVariable Long taskId) {
        TaskProgress progress = taskService.getTaskProgress(taskId);
        return mapper.toTaskProgressDto(progress);
    }
}
