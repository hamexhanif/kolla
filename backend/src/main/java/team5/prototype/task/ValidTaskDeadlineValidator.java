package team5.prototype.task;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import team5.prototype.tenant.TenantContext;
import team5.prototype.workflow.definition.WorkflowDefinitionRepository;
import team5.prototype.workflow.step.WorkflowStep;

import java.time.LocalDateTime;

public class ValidTaskDeadlineValidator implements ConstraintValidator<ValidTaskDeadline, TaskDto> {

    @Autowired
    private WorkflowDefinitionRepository workflowDefinitionRepository;

    @Override
    public boolean isValid(TaskDto taskDto, ConstraintValidatorContext context) {
        if (taskDto.getDeadline() == null || taskDto.getWorkflowDefinitionId() == null) {
            return true; // Let other validators handle null checks
        }

        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return false;
        }

        return workflowDefinitionRepository.findByIdAndTenantId(taskDto.getWorkflowDefinitionId(), tenantId)
                .map(definition -> {
                    // Calculate total duration from all workflow steps
                    long totalDurationHours = definition.getSteps().stream()
                            .mapToLong(WorkflowStep::getDurationHours)
                            .sum();

                    LocalDateTime minimumDeadline = LocalDateTime.now().plusHours(totalDurationHours);

                    // Deadline must be at or after the minimum deadline
                    return !taskDto.getDeadline().isBefore(minimumDeadline);
                })
                .orElse(false); // Invalid if workflow definition not found
    }
}
