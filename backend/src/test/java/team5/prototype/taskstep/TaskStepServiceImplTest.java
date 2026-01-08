package team5.prototype.taskstep;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team5.prototype.dto.ActorDashboardItemDto;
import team5.prototype.notification.NotificationService;
import team5.prototype.security.TenantProvider;
import team5.prototype.task.Task;
import team5.prototype.task.TaskStatus;
import team5.prototype.task.TaskService;
import team5.prototype.tenant.Tenant;
import team5.prototype.user.User;
import team5.prototype.user.UserRepository;
import team5.prototype.workflow.step.WorkflowStep;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    @Mock
    private NotificationService notificationService;
    @Mock
    private TenantProvider tenantProvider;

    @InjectMocks
    private TaskStepServiceImpl taskStepService;

    private TaskStep taskStep;
    private User user;
    private Tenant tenant;

    @BeforeEach
    void setUp() {
        tenant = Tenant.builder().id(5L).name("t1").build();
        Task task = Task.builder()
                .id(42L)
                .title("Test Task")
                .deadline(LocalDateTime.now().plusDays(1))
                .status(TaskStatus.NOT_STARTED)
                .tenant(tenant)
                .build();
        WorkflowStep workflowStep = WorkflowStep.builder()
                .id(7L)
                .name("Review")
                .sequenceOrder(1)
                .build();
        taskStep = TaskStep.builder()
                .id(1L)
                .task(task)
                .workflowStep(workflowStep)
                .status(TaskStepStatus.WAITING)
                .priority(Priority.MEDIUM_TERM)
                .build();
        user = User.builder().id(10L).build();
    }

    @Test
    void assignsTaskStepToUserAndSetsAssignedAt() {
        when(tenantProvider.getCurrentTenantId()).thenReturn(5L);
        when(taskStepRepository.findByIdAndTask_Tenant_Id(1L, 5L)).thenReturn(Optional.of(taskStep));
        when(userRepository.findByIdAndTenant_Id(10L, 5L)).thenReturn(Optional.of(user));
        when(taskStepRepository.save(any(TaskStep.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskStep result = taskStepService.assignTaskStepToUser(1L, 10L);

        assertThat(result.getAssignedUser()).isEqualTo(user);
        assertThat(result.getStatus()).isEqualTo(TaskStepStatus.ASSIGNED);
        assertThat(result.getAssignedAt()).isNotNull();
    }

    @Test
    void assignTaskStepThrowsWhenStepMissing() {
        when(tenantProvider.getCurrentTenantId()).thenReturn(5L);
        when(taskStepRepository.findByIdAndTask_Tenant_Id(1L, 5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskStepService.assignTaskStepToUser(1L, 10L))
                .isInstanceOf(jakarta.persistence.EntityNotFoundException.class);
    }

    @Test
    void assignTaskStepThrowsWhenUserMissing() {
        when(tenantProvider.getCurrentTenantId()).thenReturn(5L);
        when(taskStepRepository.findByIdAndTask_Tenant_Id(1L, 5L)).thenReturn(Optional.of(taskStep));
        when(userRepository.findByIdAndTenant_Id(10L, 5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskStepService.assignTaskStepToUser(1L, 10L))
                .isInstanceOf(jakarta.persistence.EntityNotFoundException.class);
    }

    @Test
    void setsManualPriorityAndMapsEnum() {
        when(tenantProvider.getCurrentTenantId()).thenReturn(5L);
        when(taskStepRepository.findByIdAndTask_Tenant_Id(1L, 5L)).thenReturn(Optional.of(taskStep));
        when(taskStepRepository.save(any(TaskStep.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskStepDto result = taskStepService.setManualPriorityAndConvertToDto(1L, 2);

        ArgumentCaptor<TaskStep> stepCaptor = ArgumentCaptor.forClass(TaskStep.class);
        verify(taskStepRepository).save(stepCaptor.capture());
        TaskStep saved = stepCaptor.getValue();
        assertThat(saved.getManualPriority()).isEqualTo(2);
        assertThat(saved.getPriority()).isEqualTo(Priority.MEDIUM_TERM);
        assertThat(result.getPriority()).isEqualTo(Priority.MEDIUM_TERM.name());
    }

    @Test
    void mapsImmediatePriorityForLowManualValue() {
        when(tenantProvider.getCurrentTenantId()).thenReturn(5L);
        when(taskStepRepository.findByIdAndTask_Tenant_Id(1L, 5L)).thenReturn(Optional.of(taskStep));
        when(taskStepRepository.save(any(TaskStep.class))).thenAnswer(invocation -> invocation.getArgument(0));

        taskStepService.setManualPriorityAndConvertToDto(1L, 1);

        ArgumentCaptor<TaskStep> stepCaptor = ArgumentCaptor.forClass(TaskStep.class);
        verify(taskStepRepository).save(stepCaptor.capture());
        assertThat(stepCaptor.getValue().getPriority()).isEqualTo(Priority.IMMEDIATE);
    }

    @Test
    void mapsLongTermPriorityForHighManualValue() {
        when(tenantProvider.getCurrentTenantId()).thenReturn(5L);
        when(taskStepRepository.findByIdAndTask_Tenant_Id(1L, 5L)).thenReturn(Optional.of(taskStep));
        when(taskStepRepository.save(any(TaskStep.class))).thenAnswer(invocation -> invocation.getArgument(0));

        taskStepService.setManualPriorityAndConvertToDto(1L, 3);

        ArgumentCaptor<TaskStep> stepCaptor = ArgumentCaptor.forClass(TaskStep.class);
        verify(taskStepRepository).save(stepCaptor.capture());
        assertThat(stepCaptor.getValue().getPriority()).isEqualTo(Priority.LONG_TERM);
    }

    @Test
    void setManualPrioritySendsNotificationPayload() {
        when(tenantProvider.getCurrentTenantId()).thenReturn(5L);
        when(taskStepRepository.findByIdAndTask_Tenant_Id(1L, 5L)).thenReturn(Optional.of(taskStep));
        when(taskStepRepository.save(any(TaskStep.class))).thenAnswer(invocation -> invocation.getArgument(0));

        taskStepService.setManualPriorityAndConvertToDto(1L, 2);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);
        verify(notificationService).sendTaskUpdateNotification(eq(42L), payloadCaptor.capture());
        Map<String, Object> payload = payloadCaptor.getValue();
        assertThat(payload.get("taskStepId")).isEqualTo(1L);
        assertThat(payload.get("newPriority")).isEqualTo(Priority.MEDIUM_TERM.name());
    }

    @Test
    void returnsActiveTaskStepsForUser() {
        List<TaskStep> steps = List.of(taskStep);
        when(tenantProvider.getCurrentTenantId()).thenReturn(5L);
        when(userRepository.findByIdAndTenant_Id(10L, 5L)).thenReturn(Optional.of(user));
        when(taskStepRepository.findByAssignedUserIdAndStatusNotAndTask_Tenant_Id(
                10L, TaskStepStatus.COMPLETED, 5L)).thenReturn(steps);

        List<TaskStep> result = taskStepService.getActiveTaskStepsByUser(10L);

        assertThat(result).containsExactly(taskStep);
    }

    @Test
    void getActiveTaskStepsThrowsWhenUserNotInTenant() {
        when(tenantProvider.getCurrentTenantId()).thenReturn(5L);
        when(userRepository.findByIdAndTenant_Id(10L, 5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskStepService.getActiveTaskStepsByUser(10L))
                .isInstanceOf(jakarta.persistence.EntityNotFoundException.class);
    }

    @Test
    void buildsActorDashboardItemsWithFilters() {
        Task task = Task.builder()
                .id(99L)
                .title("Task One")
                .deadline(LocalDateTime.now().plusDays(1))
                .status(TaskStatus.NOT_STARTED)
                .tenant(tenant)
                .build();
        WorkflowStep workflowStep = WorkflowStep.builder()
                .id(88L)
                .name("Review Document")
                .sequenceOrder(2)
                .build();
        TaskStep step = TaskStep.builder()
                .id(1L)
                .task(task)
                .workflowStep(workflowStep)
                .status(TaskStepStatus.ASSIGNED)
                .priority(Priority.MEDIUM_TERM)
                .assignedAt(LocalDateTime.now())
                .build();

        when(tenantProvider.getCurrentTenantId()).thenReturn(5L);
        when(userRepository.findByIdAndTenant_Id(10L, 5L)).thenReturn(Optional.of(user));
        when(taskStepRepository.findByAssignedUserIdAndStatusNotAndTask_Tenant_Id(10L, TaskStepStatus.COMPLETED, 5L))
                .thenReturn(List.of(step));

        List<ActorDashboardItemDto> result = taskStepService.getActorDashboardItems(
                10L,
                TaskStepStatus.ASSIGNED,
                Priority.MEDIUM_TERM,
                "review"
        );

        assertThat(result).hasSize(1);
        assertThat(result.get(0).taskId()).isEqualTo(99L);
        assertThat(result.get(0).stepName()).isEqualTo("Review Document");
    }

    @Test
    void delegatesCompleteTaskStepToTaskService() {
        taskStepService.completeTaskStep(5L, 1L, 10L);

        verify(taskService).completeStep(5L, 1L, 10L);
    }
}
