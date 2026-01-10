package team5.prototype.taskstep;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class TaskStepTest {

    @Test
    void onCreateSetsAssignedAtWhenStatusNotWaiting() {
        TaskStep step = TaskStep.builder()
                .status(TaskStepStatus.ASSIGNED)
                .assignedAt(null)
                .build();

        step.onCreate();

        assertThat(step.getAssignedAt()).isNotNull();
    }

    @Test
    void onCreateDoesNotOverwriteAssignedAtWhenWaiting() {
        LocalDateTime assignedAt = LocalDateTime.now().minusDays(1);
        TaskStep step = TaskStep.builder()
                .status(TaskStepStatus.WAITING)
                .assignedAt(assignedAt)
                .build();

        step.onCreate();

        assertThat(step.getAssignedAt()).isEqualTo(assignedAt);
    }
}
