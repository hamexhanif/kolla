package team5.prototype.taskstep;

import org.junit.jupiter.api.Test;
import team5.prototype.task.Task;
import team5.prototype.task.TaskStatus;
import team5.prototype.tenant.Tenant;
import team5.prototype.workflow.definition.WorkflowDefinition;
import team5.prototype.workflow.step.WorkflowStep;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PriorityServiceImplTest {

    private final PriorityServiceImpl priorityService = new PriorityServiceImpl();

    @Test
    void calculatesImmediateWhenStepDueSoon() {
        TaskStep step = buildStepWithDeadline(LocalDateTime.now().plusHours(4), 1, 1);

        Priority priority = priorityService.calculatePriority(step);

        assertThat(priority).isEqualTo(Priority.IMMEDIATE);
    }

    @Test
    void calculatesMediumWhenStepDueBetweenEightAndThirtyTwoHours() {
        TaskStep step = buildStepWithDeadline(LocalDateTime.now().plusHours(20), 1, 1);

        Priority priority = priorityService.calculatePriority(step);

        assertThat(priority).isEqualTo(Priority.MEDIUM_TERM);
    }

    @Test
    void calculatesLongWhenStepDueLater() {
        TaskStep step = buildStepWithDeadline(LocalDateTime.now().plusHours(60), 1, 1);

        Priority priority = priorityService.calculatePriority(step);

        assertThat(priority).isEqualTo(Priority.LONG_TERM);
    }

    private TaskStep buildStepWithDeadline(LocalDateTime deadline, int currentSequence, int followingDuration) {
        Tenant tenant = Tenant.builder().id(1L).name("t").build();
        WorkflowStep current = WorkflowStep.builder()
                .id(1L)
                .name("Current")
                .sequenceOrder(currentSequence)
                .durationHours(1)
                .build();
        WorkflowStep following = WorkflowStep.builder()
                .id(2L)
                .name("Next")
                .sequenceOrder(currentSequence + 1)
                .durationHours(followingDuration)
                .build();
        WorkflowDefinition definition = WorkflowDefinition.builder()
                .id(5L)
                .name("wf")
                .tenant(tenant)
                .steps(List.of(current, following))
                .build();

        Task task = Task.builder()
                .id(10L)
                .title("Task")
                .deadline(deadline)
                .status(TaskStatus.IN_PROGRESS)
                .workflowDefinition(definition)
                .tenant(tenant)
                .build();

        return TaskStep.builder()
                .id(20L)
                .task(task)
                .workflowStep(current)
                .status(TaskStepStatus.ASSIGNED)
                .build();
    }
}
