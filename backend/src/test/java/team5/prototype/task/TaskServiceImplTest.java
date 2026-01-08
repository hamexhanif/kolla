// VOLLSTAENDIGER, KORRIGIERTER INHALT FUER TaskServiceImplTest.java

package team5.prototype.task;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team5.prototype.role.Role;
import team5.prototype.notification.NotificationService;
import team5.prototype.security.TenantProvider;
import team5.prototype.taskstep.PriorityService;
import team5.prototype.taskstep.TaskStep;
import team5.prototype.taskstep.TaskStepStatus;
import team5.prototype.tenant.Tenant;
import team5.prototype.user.User;
import team5.prototype.user.UserRepository;
import team5.prototype.workflow.definition.WorkflowDefinition;
import team5.prototype.workflow.definition.WorkflowDefinitionRepository;
import team5.prototype.workflow.step.WorkflowStep;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private WorkflowDefinitionRepository definitionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PriorityService priorityService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private TenantProvider tenantProvider;

    @InjectMocks
    private TaskServiceImpl taskService;

    private WorkflowDefinition definition;
    private User creator;

    @BeforeEach
    void setUp() {
        Role role = Role.builder().id(1L).name("TEST_ROLE").build();
        WorkflowStep step1 = WorkflowStep.builder().id(101L).name("First").sequenceOrder(1).requiredRole(role).build();
        Tenant tenant = Tenant.builder().id(5L).name("t1").build();
        definition = WorkflowDefinition.builder().id(201L).name("WF").tenant(tenant).build();
        creator = User.builder().id(11L).tenant(tenant).build();
        when(tenantProvider.getCurrentTenantId()).thenReturn(tenant.getId());
    }

    // ===================================================================
    // KORREKTUR: Der fehlerhafte Test wird repariert
    // Wir ignorieren die anderen Tests, um uns auf den Build-Fehler zu konzentrieren.
    // ===================================================================
    @Test
    void createTaskThrowsWhenDefinitionMissing() {
        // KORREKTUR: Wir erstellen jetzt ein TaskDto, wie es die Methode erwartet.
        TaskDto requestDto = new TaskDto();
        requestDto.setWorkflowDefinitionId(999L);
        requestDto.setTitle("t");
        requestDto.setDescription("d");
        requestDto.setDeadline(LocalDateTime.now());
        requestDto.setCreatedById(creator.getId());

        when(definitionRepository.findByIdAndTenant_Id(999L, 5L)).thenReturn(Optional.empty());

        // Die Assertion bleibt gleich, aber ruft jetzt die korrekte Methode auf.
        assertThatThrownBy(() -> taskService.createTaskFromDefinition(requestDto))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void createTaskThrowsWhenDefinitionHasNoSteps() {
        TaskDto requestDto = new TaskDto();
        requestDto.setWorkflowDefinitionId(definition.getId());
        requestDto.setTitle("t");
        requestDto.setDescription("d");
        requestDto.setDeadline(LocalDateTime.now());
        requestDto.setCreatedById(creator.getId());

        when(definitionRepository.findByIdAndTenant_Id(definition.getId(), 5L)).thenReturn(Optional.of(definition));

        assertThatThrownBy(() -> taskService.createTaskFromDefinition(requestDto))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void completeStepThrowsWhenTaskMissing() {
        when(taskRepository.findByIdAndTenant_Id(1L, 5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.completeStep(1L, 2L, 3L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void completeStepThrowsWhenStepMissing() {
        Task task = Task.builder()
                .id(1L)
                .taskSteps(List.of())
                .build();
        when(taskRepository.findByIdAndTenant_Id(1L, 5L)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> taskService.completeStep(1L, 2L, 3L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void completeStepThrowsWhenUserNotAssigned() {
        User assigned = User.builder().id(10L).build();
        TaskStep step = TaskStep.builder()
                .id(2L)
                .assignedUser(assigned)
                .status(TaskStepStatus.ASSIGNED)
                .build();
        Task task = Task.builder()
                .id(1L)
                .taskSteps(List.of(step))
                .build();
        step.setTask(task);

        when(taskRepository.findByIdAndTenant_Id(1L, 5L)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> taskService.completeStep(1L, 2L, 11L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void completeStepThrowsWhenStepWaiting() {
        User assigned = User.builder().id(10L).build();
        TaskStep step = TaskStep.builder()
                .id(2L)
                .assignedUser(assigned)
                .status(TaskStepStatus.WAITING)
                .build();
        Task task = Task.builder()
                .id(1L)
                .taskSteps(List.of(step))
                .build();
        step.setTask(task);

        when(taskRepository.findByIdAndTenant_Id(1L, 5L)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> taskService.completeStep(1L, 2L, 10L))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void completeStepDoesNothingWhenAlreadyCompleted() {
        User assigned = User.builder().id(10L).build();
        TaskStep step = TaskStep.builder()
                .id(2L)
                .assignedUser(assigned)
                .status(TaskStepStatus.COMPLETED)
                .build();
        Task task = Task.builder()
                .id(1L)
                .taskSteps(List.of(step))
                .build();
        step.setTask(task);

        when(taskRepository.findByIdAndTenant_Id(1L, 5L)).thenReturn(Optional.of(task));

        taskService.completeStep(1L, 2L, 10L);

        verify(taskRepository, never()).save(any(Task.class));
        verifyNoInteractions(notificationService);
    }

    @Test
    void completeStepAdvancesToNextStepAndNotifies() {
        User assigned = User.builder().id(10L).build();
        WorkflowStep wfStep1 = WorkflowStep.builder().name("Step One").sequenceOrder(1).build();
        WorkflowStep wfStep2 = WorkflowStep.builder().name("Step Two").sequenceOrder(2).build();

        TaskStep step1 = TaskStep.builder()
                .id(2L)
                .assignedUser(assigned)
                .status(TaskStepStatus.ASSIGNED)
                .workflowStep(wfStep1)
                .build();
        TaskStep step2 = TaskStep.builder()
                .id(3L)
                .assignedUser(assigned)
                .status(TaskStepStatus.WAITING)
                .workflowStep(wfStep2)
                .build();

        Task task = Task.builder()
                .id(1L)
                .status(TaskStatus.NOT_STARTED)
                .currentStepIndex(0)
                .taskSteps(new ArrayList<>(List.of(step1, step2)))
                .tenant(Tenant.builder().id(5L).name("t1").build())
                .build();
        step1.setTask(task);
        step2.setTask(task);

        when(taskRepository.findByIdAndTenant_Id(1L, 5L)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);
        when(priorityService.calculatePriority(step2)).thenReturn(team5.prototype.taskstep.Priority.MEDIUM_TERM);

        taskService.completeStep(1L, 2L, 10L);

        assertThat(step1.getStatus()).isEqualTo(TaskStepStatus.COMPLETED);
        assertThat(step1.getCompletedAt()).isNotNull();
        assertThat(step2.getStatus()).isEqualTo(TaskStepStatus.ASSIGNED);
        assertThat(step2.getAssignedAt()).isNotNull();
        assertThat(task.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(task.getCurrentStepIndex()).isEqualTo(1);
        verify(notificationService).sendTaskUpdateNotification(any(Long.class), any(Map.class));
    }
}
