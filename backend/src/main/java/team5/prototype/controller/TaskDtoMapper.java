package team5.prototype.controller;

import org.springframework.stereotype.Component;
import team5.prototype.dto.TaskProgressDto;
import team5.prototype.dto.TaskResponseDto;
import team5.prototype.dto.TaskStepDto;
import team5.prototype.entity.Task;
import team5.prototype.entity.TaskStep;
import team5.prototype.entity.WorkflowStep;
import team5.prototype.service.TaskProgress;

import java.util.Comparator;
import java.util.List;

@Component
public class TaskDtoMapper {

    public TaskResponseDto toTaskResponse(Task task) {
        List<TaskStepDto> steps = task.getTaskSteps() == null
                ? List.of()
                : task.getTaskSteps().stream()
                .sorted(Comparator.comparingInt(step -> step.getWorkflowStep().getSequenceOrder()))
                .map(this::toTaskStepDto)
                .toList();

        return new TaskResponseDto(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getDeadline(),
                task.getStatus(),
                steps
        );
    }

    public TaskStepDto toTaskStepDto(TaskStep step) {
        WorkflowStep workflowStep = step.getWorkflowStep();
        Integer workflowOrder = workflowStep != null ? workflowStep.getSequenceOrder() : null;
        String workflowName = workflowStep != null ? workflowStep.getName() : null;
        Long taskId = step.getTask() != null ? step.getTask().getId() : null;
        String taskTitle = step.getTask() != null ? step.getTask().getTitle() : null;

        return new TaskStepDto(
                step.getId(),
                taskId,
                taskTitle,
                step.getAssignedUser() != null ? step.getAssignedUser().getId() : null,
                workflowName,
                workflowOrder,
                step.getStatus(),
                step.getPriority(),
                step.getManualPriority(),
                step.getAssignedAt(),
                step.getCompletedAt()
        );
    }

    public TaskProgressDto toTaskProgressDto(TaskProgress taskProgress) {
        return new TaskProgressDto(
                taskProgress.taskId(),
                taskProgress.title(),
                taskProgress.deadline(),
                taskProgress.totalSteps(),
                taskProgress.completedSteps(),
                taskProgress.status()
        );
    }
}
