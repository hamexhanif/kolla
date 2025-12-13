package team5.prototype.service;

import org.junit.jupiter.api.Test;
import team5.prototype.entity.*;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PriorityServiceImplTest {

    private final Clock clock = Clock.fixed(Instant.parse("2025-01-01T00:00:00Z"), ZoneOffset.UTC);
    private final PriorityServiceImpl service = new PriorityServiceImpl(clock);

    @Test
    void returnsImmediateWhenSlackBelowThreshold() {
        Task task = taskWithDeadlineAndDurations(16, List.of(10));
        assertThat(service.calculatePriority(task)).isEqualTo(Priority.IMMEDIATE);
    }

    @Test
    void returnsMediumWhenSlackUnderThirtyTwoHours() {
        Task task = taskWithDeadlineAndDurations(50, List.of(20));
        assertThat(service.calculatePriority(task)).isEqualTo(Priority.MEDIUM_TERM);
    }

    @Test
    void returnsLongWhenPlentyOfTimeLeft() {
        Task task = taskWithDeadlineAndDurations(120, List.of(10));
        assertThat(service.calculatePriority(task)).isEqualTo(Priority.LONG_TERM);
    }

    @Test
    void overdueTasksAreAlwaysImmediate() {
        Task task = taskWithDeadlineAndDurations(-2, List.of(1));
        assertThat(service.calculatePriority(task)).isEqualTo(Priority.IMMEDIATE);
    }

    private Task taskWithDeadlineAndDurations(long hoursUntilDeadline, List<Integer> durations) {
        LocalDateTime deadline = LocalDateTime.ofInstant(clock.instant(), clock.getZone()).plusHours(hoursUntilDeadline);
        Task task = Task.builder()
                .deadline(deadline)
                .taskSteps(durations.stream()
                        .map(duration -> TaskStep.builder()
                                .status(TaskStepStatus.ASSIGNED)
                                .workflowStep(WorkflowStep.builder()
                                        .durationHours(duration)
                                        .build())
                                .build())
                        .toList())
                .build();
        return task;
    }
}
