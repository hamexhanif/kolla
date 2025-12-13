package team5.prototype.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import team5.prototype.entity.*;
import team5.prototype.repository.TaskStepRepository;
import team5.prototype.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final TaskStepRepository taskStepRepository = mock(TaskStepRepository.class);

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository, taskStepRepository);
    }

    @Test
    void returnsOnlyActiveStepsSortedByManualPriority() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(User.builder().id(1L).build()));
        TaskStep manual = TaskStep.builder()
                .id(10L)
                .status(TaskStepStatus.ASSIGNED)
                .priority(Priority.MEDIUM_TERM)
                .manualPriority(1)
                .assignedAt(LocalDateTime.now())
                .workflowStep(WorkflowStep.builder().sequenceOrder(1).build())
                .build();
        TaskStep automatic = TaskStep.builder()
                .id(11L)
                .status(TaskStepStatus.ASSIGNED)
                .priority(Priority.IMMEDIATE)
                .assignedAt(LocalDateTime.now().plusMinutes(1))
                .workflowStep(WorkflowStep.builder().sequenceOrder(2).build())
                .build();
        TaskStep waiting = TaskStep.builder()
                .id(12L)
                .status(TaskStepStatus.WAITING)
                .priority(Priority.LONG_TERM)
                .workflowStep(WorkflowStep.builder().sequenceOrder(3).build())
                .build();

        when(taskStepRepository.findByAssignedUserIdAndStatusNot(1L, TaskStepStatus.COMPLETED))
                .thenReturn(List.of(automatic, waiting, manual));

        List<TaskStep> steps = userService.getActiveStepsForUser(1L);

        assertThat(steps).containsExactly(manual, automatic);
    }

    @Test
    void overrideManualPriorityUpdatesStep() {
        TaskStep step = TaskStep.builder()
                .id(22L)
                .status(TaskStepStatus.ASSIGNED)
                .workflowStep(WorkflowStep.builder().sequenceOrder(1).build())
                .build();
        when(taskStepRepository.findById(step.getId())).thenReturn(Optional.of(step));
        when(taskStepRepository.save(any(TaskStep.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskStep updated = userService.overrideManualPriority(step.getId(), 5);
        assertThat(updated.getManualPriority()).isEqualTo(5);
    }

    @Test
    void throwsWhenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.getActiveStepsForUser(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
