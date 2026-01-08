package team5.prototype.taskstep;

import org.junit.jupiter.api.Test;
import team5.prototype.task.Task;
import team5.prototype.workflow.step.WorkflowStep;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PriorityServiceImplTest {

    private final PriorityServiceImpl priorityService = new PriorityServiceImpl();

    @Test
    void returnsImmediateWhenDeadlinePassed() {
        Task task = Task.builder()
                .deadline(LocalDateTime.now().minusHours(1))
                .build();
        TaskStep step = TaskStep.builder()
                .task(task)
                .status(TaskStepStatus.ASSIGNED)
                .workflowStep(WorkflowStep.builder().durationHours(1).build())
                .build();
        task.setTaskSteps(List.of(step));

        Priority priority = priorityService.calculatePriority(step);

        assertThat(priority).isEqualTo(Priority.IMMEDIATE);
    }

    @Test
    void returnsImmediateWhenSlackIsSmall() {
        Task task = Task.builder()
                .deadline(LocalDateTime.now().plusHours(10))
                .build();
        TaskStep step = TaskStep.builder()
                .task(task)
                .status(TaskStepStatus.ASSIGNED)
                .workflowStep(WorkflowStep.builder().durationHours(9).build())
                .build();
        task.setTaskSteps(List.of(step));

        Priority priority = priorityService.calculatePriority(step);

        assertThat(priority).isEqualTo(Priority.IMMEDIATE);
    }

    @Test
    void returnsMediumTermWhenSlackIsWithinThreshold() {
        Task task = Task.builder()
                .deadline(LocalDateTime.now().plusHours(40))
                .build();
        TaskStep step = TaskStep.builder()
                .task(task)
                .status(TaskStepStatus.ASSIGNED)
                .workflowStep(WorkflowStep.builder().durationHours(12).build())
                .build();
        task.setTaskSteps(List.of(step));

        Priority priority = priorityService.calculatePriority(step);

        assertThat(priority).isEqualTo(Priority.MEDIUM_TERM);
    }

    @Test
    void returnsLongTermWhenSlackIsLarge() {
        Task task = Task.builder()
                .deadline(LocalDateTime.now().plusHours(80))
                .build();
        TaskStep step = TaskStep.builder()
                .task(task)
                .status(TaskStepStatus.ASSIGNED)
                .workflowStep(WorkflowStep.builder().durationHours(8).build())
                .build();
        task.setTaskSteps(List.of(step));

        Priority priority = priorityService.calculatePriority(step);

        assertThat(priority).isEqualTo(Priority.LONG_TERM);
    }
}
