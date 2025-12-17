package team5.prototype.taskstep;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team5.prototype.task.TaskService;
import team5.prototype.user.User;
import team5.prototype.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskStepServiceImplTest {

    @Mock
    private TaskStepRepository taskStepRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskStepServiceImpl taskStepService;

    private TaskStep taskStep;
    private User user;

    @BeforeEach
    void setUp() {
        taskStep = TaskStep.builder()
                .id(1L)
                .status(TaskStepStatus.WAITING)
                .priority(Priority.MEDIUM_TERM)
                .build();
        user = User.builder().id(10L).build();
    }

    @Test
    void assignsTaskStepToUserAndSetsAssignedAt() {
        when(taskStepRepository.findById(1L)).thenReturn(Optional.of(taskStep));
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(taskStepRepository.save(any(TaskStep.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskStep result = taskStepService.assignTaskStepToUser(1L, 10L);

        assertThat(result.getAssignedUser()).isEqualTo(user);
        assertThat(result.getStatus()).isEqualTo(TaskStepStatus.ASSIGNED);
        assertThat(result.getAssignedAt()).isNotNull();
    }

    @Test
    void setsManualPriorityAndMapsEnum() {
        when(taskStepRepository.findById(1L)).thenReturn(Optional.of(taskStep));
        when(taskStepRepository.save(any(TaskStep.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskStep result = taskStepService.setManualPriority(1L, 2);

        assertThat(result.getManualPriority()).isEqualTo(2);
        assertThat(result.getPriority()).isEqualTo(Priority.MEDIUM_TERM);
    }

    @Test
    void returnsActiveTaskStepsForUser() {
        List<TaskStep> steps = List.of(taskStep);
        when(taskStepRepository.findByAssignedUserIdAndStatusNot(10L, TaskStepStatus.COMPLETED)).thenReturn(steps);

        List<TaskStep> result = taskStepService.getActiveTaskStepsByUser(10L);

        assertThat(result).containsExactly(taskStep);
    }

    @Test
    void delegatesCompleteTaskStepToTaskService() {
        taskStepService.completeTaskStep(5L, 1L, 10L);

        verify(taskService).completeStep(5L, 1L, 10L);
    }
}
