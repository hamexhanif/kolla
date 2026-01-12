package team5.prototype.taskstep;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team5.prototype.dto.ActorDashboardItemDto;
import team5.prototype.notification.NotificationService;
import team5.prototype.task.Task;
import team5.prototype.task.TaskStatus;
import team5.prototype.task.TaskService;
import team5.prototype.workflow.step.WorkflowStep;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskStepServiceImplTest {

    @Mock
    private TaskStepRepository taskStepRepository;
    @Mock
    private TaskService taskService;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private TaskStepServiceImpl taskStepService;

    @Test
    void getActorDashboardItemsFiltersAndMaps() {
        Task task = Task.builder()
                .id(11L)
                .title("Review Document")
                .deadline(LocalDateTime.now().plusDays(1))
                .status(TaskStatus.IN_PROGRESS)
                .build();
        WorkflowStep workflowStep = WorkflowStep.builder()
                .id(21L)
                .name("Review")
                .sequenceOrder(1)
                .build();
        TaskStep matching = TaskStep.builder()
                .id(31L)
                .task(task)
                .workflowStep(workflowStep)
                .status(TaskStepStatus.ASSIGNED)
                .priority(Priority.MEDIUM_TERM)
                .assignedAt(LocalDateTime.now())
                .build();
        TaskStep ignored = TaskStep.builder()
                .id(32L)
                .task(task)
                .workflowStep(workflowStep)
                .status(TaskStepStatus.COMPLETED)
                .priority(Priority.LONG_TERM)
                .build();

        when(taskStepRepository.findByAssignedUserIdAndStatusNot(5L, TaskStepStatus.COMPLETED))
                .thenReturn(List.of(matching, ignored));

        List<ActorDashboardItemDto> result = taskStepService.getActorDashboardItems(
                5L,
                TaskStepStatus.ASSIGNED,
                Priority.MEDIUM_TERM,
                "review"
        );

        assertThat(result).hasSize(1);
        assertThat(result.get(0).taskId()).isEqualTo(11L);
        assertThat(result.get(0).stepName()).isEqualTo("Review");
    }

    @Test
    void setManualPriorityUpdatesAndNotifies() {
        Task task = Task.builder().id(91L).build();
        WorkflowStep workflowStep = WorkflowStep.builder().name("Finalize").build();
        TaskStep step = TaskStep.builder()
                .id(41L)
                .task(task)
                .workflowStep(workflowStep)
                .status(TaskStepStatus.ASSIGNED)
                .priority(Priority.MEDIUM_TERM)
                .build();

        when(taskStepRepository.findById(41L)).thenReturn(Optional.of(step));
        when(taskStepRepository.save(any(TaskStep.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskStepDto dto = taskStepService.setManualPriorityAndConvertToDto(41L, 3);

        assertThat(step.getManualPriority()).isEqualTo(3);
        assertThat(step.getPriority()).isEqualTo(Priority.LONG_TERM);
        assertThat(dto.getId()).isEqualTo(41L);
        assertThat(dto.getPriority()).isEqualTo(Priority.LONG_TERM.name());

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(notificationService).sendTaskUpdateNotification(
                org.mockito.ArgumentMatchers.eq(91L),
                payloadCaptor.capture()
        );
        @SuppressWarnings("unchecked")
        Map<String, Object> payload = (Map<String, Object>) payloadCaptor.getValue();
        assertThat(payload).containsEntry("newPriority", Priority.LONG_TERM.name());
    }

    @Test
    void completeTaskStepDelegatesToTaskService() {
        taskStepService.completeTaskStep(10L, 20L, 30L);

        verify(taskService).completeStep(10L, 20L, 30L);
    }

    @Test
    void getAllTaskStepsByUserIdReturnsRepositoryData() {
        TaskStep step = TaskStep.builder().id(77L).build();
        when(taskStepRepository.findAllByAssignedUserId(7L)).thenReturn(List.of(step));

        List<TaskStep> result = taskStepService.getAllTaskStepsByUserId(7L);

        assertThat(result).containsExactly(step);
    }
}
