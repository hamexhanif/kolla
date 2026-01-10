// VOLLSTAENDIGER, KORRIGIERTER INHALT FUER TaskServiceImplTest.java

package team5.prototype.task;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team5.prototype.dto.ManagerDashboardDto;
import team5.prototype.dto.TaskDetailsDto;
import team5.prototype.role.Role;
import team5.prototype.notification.NotificationService;
import team5.prototype.security.TenantProvider;
import team5.prototype.taskstep.Priority;
import team5.prototype.taskstep.PriorityService;
import team5.prototype.taskstep.TaskStep;
import team5.prototype.taskstep.TaskStepStatus;
import team5.prototype.taskstep.TaskStepRepository;
import team5.prototype.tenant.Tenant;
import team5.prototype.user.User;
import team5.prototype.user.UserRepository;
import team5.prototype.workflow.definition.WorkflowDefinition;
import team5.prototype.workflow.definition.WorkflowDefinitionRepository;
import team5.prototype.workflow.step.WorkflowStep;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

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
    private TaskStepRepository taskStepRepository;
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
        Role role = Role.builder().id(2L).name("DEV").build();
        User assigned = User.builder().id(10L).roles(Set.of(role)).build();
        WorkflowStep workflowStep = WorkflowStep.builder().name("Review").requiredRole(role).build();
        TaskStep step = TaskStep.builder()
                .id(2L)
                .assignedUser(assigned)
                .status(TaskStepStatus.WAITING)
                .workflowStep(workflowStep)
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
    void completeStepThrowsWhenUserLacksRequiredRole() {
        Role requiredRole = Role.builder().id(2L).name("DEV").build();
        Role otherRole = Role.builder().id(3L).name("QA").build();
        User assigned = User.builder().id(10L).roles(Set.of(otherRole)).build();
        WorkflowStep workflowStep = WorkflowStep.builder().name("Review").requiredRole(requiredRole).build();
        TaskStep step = TaskStep.builder()
                .id(2L)
                .assignedUser(assigned)
                .status(TaskStepStatus.ASSIGNED)
                .workflowStep(workflowStep)
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
        Role role = Role.builder().id(2L).name("DEV").build();
        User assigned = User.builder().id(10L).roles(Set.of(role)).build();
        WorkflowStep workflowStep = WorkflowStep.builder().name("Review").requiredRole(role).build();
        TaskStep step = TaskStep.builder()
                .id(2L)
                .assignedUser(assigned)
                .status(TaskStepStatus.COMPLETED)
                .workflowStep(workflowStep)
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
        Role role = Role.builder().id(2L).name("DEV").build();
        User assigned = User.builder().id(10L).roles(Set.of(role)).build();
        WorkflowStep wfStep1 = WorkflowStep.builder()
                .name("Step One")
                .sequenceOrder(1)
                .requiredRole(role)
                .build();
        WorkflowStep wfStep2 = WorkflowStep.builder()
                .name("Step Two")
                .sequenceOrder(2)
                .requiredRole(role)
                .build();

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

    @Test
    void getAllTasksUsesTenantScope() {
        Task task = Task.builder().id(1L).build();

        when(taskRepository.findAllByTenant_Id(5L)).thenReturn(List.of(task));

        List<Task> tasks = taskService.getAllTasks();

        assertThat(tasks).containsExactly(task);
        verify(taskRepository).findAllByTenant_Id(5L);
    }

    @Test
    void getTaskByIdUsesTenantScope() {
        Task task = Task.builder().id(1L).build();

        when(taskRepository.findByIdAndTenant_Id(1L, 5L)).thenReturn(Optional.of(task));

        Optional<Task> result = taskService.getTaskById(1L);

        assertThat(result).contains(task);
        verify(taskRepository).findByIdAndTenant_Id(1L, 5L);
    }

    @Test
    void getAllTasksAsDtoHandlesNullStatusAndSteps() {
        Task task = Task.builder()
                .id(1L)
                .title("Task")
                .status(null)
                .taskSteps(null)
                .build();

        when(taskRepository.findAllByTenant_Id(5L)).thenReturn(List.of(task));

        List<TaskDto> result = taskService.getAllTasksAsDto();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isNull();
        assertThat(result.get(0).getSteps()).isNull();
    }

    @Test
    void getTaskByIdAsDtoMapsStepsWithoutAssignedUser() {
        WorkflowStep wfStep = WorkflowStep.builder()
                .id(101L)
                .name("Step")
                .sequenceOrder(1)
                .build();
        TaskStep step = TaskStep.builder()
                .id(11L)
                .workflowStep(wfStep)
                .status(null)
                .assignedUser(null)
                .build();
        Task task = Task.builder()
                .id(1L)
                .title("Task")
                .status(TaskStatus.NOT_STARTED)
                .taskSteps(List.of(step))
                .build();

        when(taskRepository.findByIdAndTenant_Id(1L, 5L)).thenReturn(Optional.of(task));

        Optional<TaskDto> result = taskService.getTaskByIdAsDto(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getSteps()).hasSize(1);
        assertThat(result.get().getSteps().get(0).getAssignedUsername()).isNull();
        assertThat(result.get().getSteps().get(0).getStatus()).isNull();
    }

    @Test
    void createTaskThrowsWhenDeadlineTooEarly() {
        WorkflowStep step = WorkflowStep.builder()
                .id(101L)
                .sequenceOrder(1)
                .durationHours(1)
                .requiredRole(Role.builder().id(1L).name("DEV").build())
                .build();
        definition.setSteps(List.of(step));

        TaskDto requestDto = new TaskDto();
        requestDto.setWorkflowDefinitionId(definition.getId());
        requestDto.setTitle("t");
        requestDto.setDescription("d");
        requestDto.setDeadline(LocalDateTime.now());
        requestDto.setCreatedById(creator.getId());

        when(definitionRepository.findByIdAndTenant_Id(definition.getId(), 5L)).thenReturn(Optional.of(definition));

        assertThatThrownBy(() -> taskService.createTaskFromDefinition(requestDto))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void createTaskThrowsWhenNoUserAvailableForRole() {
        Role role = Role.builder().id(1L).name("DEV").build();
        WorkflowStep step = WorkflowStep.builder()
                .id(101L)
                .sequenceOrder(1)
                .durationHours(1)
                .requiredRole(role)
                .build();
        definition.setSteps(List.of(step));

        TaskDto requestDto = new TaskDto();
        requestDto.setWorkflowDefinitionId(definition.getId());
        requestDto.setTitle("t");
        requestDto.setDescription("d");
        requestDto.setDeadline(LocalDateTime.now().plusHours(2));
        requestDto.setCreatedById(creator.getId());

        when(definitionRepository.findByIdAndTenant_Id(definition.getId(), 5L)).thenReturn(Optional.of(definition));
        when(userRepository.findByIdAndTenant_Id(creator.getId(), 5L)).thenReturn(Optional.of(creator));
        when(userRepository.findActiveUsersByRoleAndTenant("DEV", 5L)).thenReturn(List.of());

        assertThatThrownBy(() -> taskService.createTaskFromDefinition(requestDto))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void createTaskAssignsStepsAndSetsPriority() {
        Role role = Role.builder().id(1L).name("DEV").build();
        WorkflowStep step1 = WorkflowStep.builder()
                .id(101L)
                .name("First")
                .sequenceOrder(1)
                .durationHours(1)
                .requiredRole(role)
                .build();
        WorkflowStep step2 = WorkflowStep.builder()
                .id(102L)
                .name("Second")
                .sequenceOrder(2)
                .durationHours(2)
                .requiredRole(role)
                .build();
        definition.setSteps(List.of(step1, step2));

        TaskDto requestDto = new TaskDto();
        requestDto.setWorkflowDefinitionId(definition.getId());
        requestDto.setTitle("t");
        requestDto.setDescription("d");
        requestDto.setDeadline(LocalDateTime.now().plusHours(5));
        requestDto.setCreatedById(creator.getId());

        User assignee = User.builder().id(22L).username("dev").build();

        when(definitionRepository.findByIdAndTenant_Id(definition.getId(), 5L)).thenReturn(Optional.of(definition));
        when(userRepository.findByIdAndTenant_Id(creator.getId(), 5L)).thenReturn(Optional.of(creator));
        when(userRepository.findActiveUsersByRoleAndTenant("DEV", 5L)).thenReturn(List.of(assignee));
        when(taskStepRepository.findActiveTaskStepsByUser(assignee.getId())).thenReturn(List.of());
        when(priorityService.calculatePriority(any(TaskStep.class))).thenReturn(Priority.IMMEDIATE);
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Task saved = taskService.createTaskFromDefinition(requestDto);

        assertThat(saved.getTaskSteps()).hasSize(2);
        assertThat(saved.getTaskSteps().get(0).getStatus()).isEqualTo(TaskStepStatus.ASSIGNED);
        assertThat(saved.getTaskSteps().get(0).getAssignedAt()).isNotNull();
        assertThat(saved.getTaskSteps().get(1).getStatus()).isEqualTo(TaskStepStatus.WAITING);
    }

    @Test
    void createTaskPrefersAvailableUserWithNoActiveTasks() {
        Role role = Role.builder().id(1L).name("DEV").build();
        WorkflowStep step = WorkflowStep.builder()
                .id(101L)
                .name("Only")
                .sequenceOrder(1)
                .durationHours(1)
                .requiredRole(role)
                .build();
        definition.setSteps(List.of(step));

        TaskDto requestDto = new TaskDto();
        requestDto.setWorkflowDefinitionId(definition.getId());
        requestDto.setTitle("t");
        requestDto.setDescription("d");
        requestDto.setDeadline(LocalDateTime.now().plusHours(2));
        requestDto.setCreatedById(creator.getId());

        User available = User.builder().id(22L).username("free").build();
        User busy = User.builder().id(23L).username("busy").build();
        TaskStep busyStep = TaskStep.builder()
                .task(Task.builder().deadline(LocalDateTime.now().plusHours(5)).build())
                .build();

        when(definitionRepository.findByIdAndTenant_Id(definition.getId(), 5L)).thenReturn(Optional.of(definition));
        when(userRepository.findByIdAndTenant_Id(creator.getId(), 5L)).thenReturn(Optional.of(creator));
        when(userRepository.findActiveUsersByRoleAndTenant("DEV", 5L)).thenReturn(List.of(available, busy));
        when(taskStepRepository.findActiveTaskStepsByUser(available.getId())).thenReturn(List.of());
        when(taskStepRepository.findActiveTaskStepsByUser(busy.getId())).thenReturn(List.of(busyStep));
        when(priorityService.calculatePriority(any(TaskStep.class))).thenReturn(Priority.MEDIUM_TERM);
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Task saved = taskService.createTaskFromDefinition(requestDto);

        assertThat(saved.getTaskSteps()).hasSize(1);
        assertThat(saved.getTaskSteps().get(0).getAssignedUser()).isEqualTo(available);
    }

    @Test
    void createTaskSelectsAssigneeWithLatestDeadlineWhenTied() {
        Role role = Role.builder().id(1L).name("DEV").build();
        WorkflowStep step = WorkflowStep.builder()
                .id(101L)
                .name("Only")
                .sequenceOrder(1)
                .durationHours(1)
                .requiredRole(role)
                .build();
        definition.setSteps(List.of(step));

        TaskDto requestDto = new TaskDto();
        requestDto.setWorkflowDefinitionId(definition.getId());
        requestDto.setTitle("t");
        requestDto.setDescription("d");
        requestDto.setDeadline(LocalDateTime.now().plusHours(3));
        requestDto.setCreatedById(creator.getId());

        User earlyUser = User.builder().id(22L).username("early").build();
        User lateUser = User.builder().id(23L).username("late").build();

        TaskStep earlyStep = TaskStep.builder()
                .task(Task.builder().deadline(LocalDateTime.now().plusHours(2)).build())
                .build();
        TaskStep lateStep = TaskStep.builder()
                .task(Task.builder().deadline(LocalDateTime.now().plusHours(5)).build())
                .build();

        when(definitionRepository.findByIdAndTenant_Id(definition.getId(), 5L)).thenReturn(Optional.of(definition));
        when(userRepository.findByIdAndTenant_Id(creator.getId(), 5L)).thenReturn(Optional.of(creator));
        when(userRepository.findActiveUsersByRoleAndTenant("DEV", 5L)).thenReturn(List.of(earlyUser, lateUser));
        when(taskStepRepository.findActiveTaskStepsByUser(earlyUser.getId())).thenReturn(List.of(earlyStep));
        when(taskStepRepository.findActiveTaskStepsByUser(lateUser.getId())).thenReturn(List.of(lateStep));
        when(priorityService.calculatePriority(any(TaskStep.class))).thenReturn(Priority.MEDIUM_TERM);
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Task saved = taskService.createTaskFromDefinition(requestDto);

        assertThat(saved.getTaskSteps()).hasSize(1);
        assertThat(saved.getTaskSteps().get(0).getAssignedUser()).isEqualTo(lateUser);
    }

    @Test
    void createTaskUsesOverridesForAssignee() {
        Role role = Role.builder().id(1L).name("DEV").build();
        WorkflowStep step = WorkflowStep.builder()
                .id(101L)
                .name("Only")
                .sequenceOrder(1)
                .durationHours(1)
                .requiredRole(role)
                .build();
        definition.setSteps(List.of(step));

        TaskDto requestDto = new TaskDto();
        requestDto.setWorkflowDefinitionId(definition.getId());
        requestDto.setTitle("t");
        requestDto.setDescription("d");
        requestDto.setDeadline(LocalDateTime.now().plusHours(2));
        requestDto.setCreatedById(creator.getId());
        requestDto.setStepAssignments(Map.of(101L, 33L));

        User overrideUser = User.builder().id(33L).username("override").build();

        when(definitionRepository.findByIdAndTenant_Id(definition.getId(), 5L)).thenReturn(Optional.of(definition));
        when(userRepository.findByIdAndTenant_Id(creator.getId(), 5L)).thenReturn(Optional.of(creator));
        when(userRepository.findByIdAndTenant_Id(33L, 5L)).thenReturn(Optional.of(overrideUser));
        when(priorityService.calculatePriority(any(TaskStep.class))).thenReturn(Priority.MEDIUM_TERM);
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Task saved = taskService.createTaskFromDefinition(requestDto);

        assertThat(saved.getTaskSteps()).hasSize(1);
        assertThat(saved.getTaskSteps().get(0).getAssignedUser()).isEqualTo(overrideUser);
        verify(userRepository).findByIdAndTenant_Id(33L, 5L);
        verify(userRepository, never()).findActiveUsersByRoleAndTenant(any(String.class), any(Long.class));
    }

    @Test
    void getManagerDashboardCalculatesCounts() {
        TaskStep openStep = TaskStep.builder().status(TaskStepStatus.ASSIGNED).priority(Priority.IMMEDIATE).build();
        TaskStep todayStep = TaskStep.builder().status(TaskStepStatus.ASSIGNED).priority(Priority.MEDIUM_TERM).build();
        TaskStep futureStep = TaskStep.builder().status(TaskStepStatus.ASSIGNED).priority(Priority.LONG_TERM).build();

        Task overdue = Task.builder()
                .id(1L)
                .status(TaskStatus.NOT_STARTED)
                .deadline(LocalDate.now().minusDays(1).atStartOfDay())
                .taskSteps(List.of(openStep))
                .build();
        Task dueToday = Task.builder()
                .id(2L)
                .status(TaskStatus.IN_PROGRESS)
                .deadline(LocalDate.now().atStartOfDay())
                .taskSteps(List.of(todayStep))
                .build();
        Task future = Task.builder()
                .id(3L)
                .status(TaskStatus.IN_PROGRESS)
                .deadline(LocalDate.now().plusDays(1).atStartOfDay())
                .taskSteps(List.of(futureStep))
                .build();
        Task completed = Task.builder()
                .id(4L)
                .status(TaskStatus.COMPLETED)
                .deadline(LocalDate.now().minusDays(2).atStartOfDay())
                .taskSteps(List.of())
                .build();

        when(taskRepository.findAllByTenant_Id(5L)).thenReturn(List.of(overdue, dueToday, future, completed));

        ManagerDashboardDto dashboard = taskService.getManagerDashboard();

        assertThat(dashboard.openTasks()).isEqualTo(3);
        assertThat(dashboard.overdueTasks()).isEqualTo(1);
        assertThat(dashboard.dueTodayTasks()).isEqualTo(1);
        assertThat(dashboard.tasks()).hasSize(4);
    }

    @Test
    void getTaskDetailsCalculatesDueDates() {
        Role role = Role.builder().id(1L).name("DEV").build();
        WorkflowStep wfStep1 = WorkflowStep.builder()
                .id(101L)
                .name("Step One")
                .sequenceOrder(1)
                .durationHours(2)
                .requiredRole(role)
                .build();
        WorkflowStep wfStep2 = WorkflowStep.builder()
                .id(102L)
                .name("Step Two")
                .sequenceOrder(2)
                .durationHours(3)
                .requiredRole(role)
                .build();
        WorkflowDefinition wf = WorkflowDefinition.builder()
                .steps(List.of(wfStep1, wfStep2))
                .build();

        User assignee = User.builder().firstName("Ada").lastName("Lovelace").username("ada").build();
        TaskStep step1 = TaskStep.builder()
                .id(11L)
                .status(TaskStepStatus.ASSIGNED)
                .priority(Priority.IMMEDIATE)
                .assignedUser(assignee)
                .workflowStep(wfStep1)
                .build();
        TaskStep step2 = TaskStep.builder()
                .id(12L)
                .status(TaskStepStatus.WAITING)
                .priority(Priority.MEDIUM_TERM)
                .assignedUser(assignee)
                .workflowStep(wfStep2)
                .build();
        LocalDateTime deadline = LocalDateTime.now().plusHours(10);
        Task task = Task.builder()
                .id(1L)
                .title("Task")
                .deadline(deadline)
                .workflowDefinition(wf)
                .taskSteps(List.of(step1, step2))
                .build();
        step1.setTask(task);
        step2.setTask(task);

        when(taskRepository.findByIdAndTenant_Id(1L, 5L)).thenReturn(Optional.of(task));

        TaskDetailsDto details = taskService.getTaskDetails(1L);

        assertThat(details.totalSteps()).isEqualTo(2);
        assertThat(details.completedSteps()).isEqualTo(0);
        assertThat(details.steps().get(0).dueDate()).isEqualTo(deadline.minusHours(3));
        assertThat(details.steps().get(0).assigneeName()).isEqualTo("Ada Lovelace");
    }

    @Test
    void getTaskDetailsUsesUsernameWhenNameMissing() {
        Role role = Role.builder().id(1L).name("DEV").build();
        WorkflowStep wfStep = WorkflowStep.builder()
                .id(101L)
                .name("Step")
                .sequenceOrder(1)
                .durationHours(1)
                .requiredRole(role)
                .build();
        WorkflowDefinition wf = WorkflowDefinition.builder()
                .steps(List.of(wfStep))
                .build();

        User assignee = User.builder().username("ada").firstName(" ").lastName(" ").build();
        TaskStep step = TaskStep.builder()
                .id(11L)
                .status(TaskStepStatus.ASSIGNED)
                .priority(Priority.IMMEDIATE)
                .assignedUser(assignee)
                .workflowStep(wfStep)
                .build();
        Task task = Task.builder()
                .id(1L)
                .title("Task")
                .deadline(LocalDateTime.now().plusHours(2))
                .workflowDefinition(wf)
                .taskSteps(List.of(step))
                .build();
        step.setTask(task);

        when(taskRepository.findByIdAndTenant_Id(1L, 5L)).thenReturn(Optional.of(task));

        TaskDetailsDto details = taskService.getTaskDetails(1L);

        assertThat(details.steps().get(0).assigneeName()).isEqualTo("ada");
    }

    @Test
    void getTaskDetailsReturnsNullAssigneeWhenMissing() {
        Role role = Role.builder().id(1L).name("DEV").build();
        WorkflowStep wfStep = WorkflowStep.builder()
                .id(101L)
                .name("Step")
                .sequenceOrder(1)
                .durationHours(1)
                .requiredRole(role)
                .build();
        WorkflowDefinition wf = WorkflowDefinition.builder()
                .steps(List.of(wfStep))
                .build();

        TaskStep step = TaskStep.builder()
                .id(11L)
                .status(TaskStepStatus.ASSIGNED)
                .priority(Priority.IMMEDIATE)
                .assignedUser(null)
                .workflowStep(wfStep)
                .build();
        Task task = Task.builder()
                .id(1L)
                .title("Task")
                .deadline(LocalDateTime.now().plusHours(2))
                .workflowDefinition(wf)
                .taskSteps(List.of(step))
                .build();
        step.setTask(task);

        when(taskRepository.findByIdAndTenant_Id(1L, 5L)).thenReturn(Optional.of(task));

        TaskDetailsDto details = taskService.getTaskDetails(1L);

        assertThat(details.steps().get(0).assigneeName()).isNull();
    }

    @Test
    void getTaskDetailsReturnsNullPriorityWhenAllStepsCompleted() {
        Role role = Role.builder().id(1L).name("DEV").build();
        WorkflowStep wfStep = WorkflowStep.builder()
                .id(101L)
                .name("Step")
                .sequenceOrder(1)
                .durationHours(1)
                .requiredRole(role)
                .build();
        WorkflowDefinition wf = WorkflowDefinition.builder()
                .steps(List.of(wfStep))
                .build();

        TaskStep step = TaskStep.builder()
                .id(11L)
                .status(TaskStepStatus.COMPLETED)
                .priority(Priority.IMMEDIATE)
                .workflowStep(wfStep)
                .build();
        Task task = Task.builder()
                .id(1L)
                .title("Task")
                .deadline(LocalDateTime.now().plusHours(2))
                .workflowDefinition(wf)
                .taskSteps(List.of(step))
                .build();
        step.setTask(task);

        when(taskRepository.findByIdAndTenant_Id(1L, 5L)).thenReturn(Optional.of(task));

        TaskDetailsDto details = taskService.getTaskDetails(1L);

        assertThat(details.priority()).isNull();
    }

    @Test
    void getTaskDetailsReturnsNullDueDateWhenDeadlineMissing() {
        Role role = Role.builder().id(1L).name("DEV").build();
        WorkflowStep wfStep = WorkflowStep.builder()
                .id(101L)
                .name("Step")
                .sequenceOrder(1)
                .durationHours(1)
                .requiredRole(role)
                .build();
        WorkflowDefinition wf = WorkflowDefinition.builder()
                .steps(List.of(wfStep))
                .build();

        TaskStep step = TaskStep.builder()
                .id(11L)
                .status(TaskStepStatus.ASSIGNED)
                .priority(Priority.IMMEDIATE)
                .workflowStep(wfStep)
                .build();
        Task task = Task.builder()
                .id(1L)
                .title("Task")
                .deadline(null)
                .workflowDefinition(wf)
                .taskSteps(List.of(step))
                .build();
        step.setTask(task);

        when(taskRepository.findByIdAndTenant_Id(1L, 5L)).thenReturn(Optional.of(task));

        TaskDetailsDto details = taskService.getTaskDetails(1L);

        assertThat(details.steps().get(0).dueDate()).isNull();
    }

    @Test
    void getTaskProgressCountsCompletedSteps() {
        TaskStep completed = TaskStep.builder().status(TaskStepStatus.COMPLETED).build();
        TaskStep assigned = TaskStep.builder().status(TaskStepStatus.ASSIGNED).build();
        Task task = Task.builder()
                .id(1L)
                .title("Task")
                .deadline(LocalDateTime.now().plusHours(2))
                .status(TaskStatus.IN_PROGRESS)
                .taskSteps(List.of(completed, assigned))
                .build();

        when(taskRepository.findByIdAndTenant_Id(1L, 5L)).thenReturn(Optional.of(task));

        TaskProgress progress = taskService.getTaskProgress(1L);

        assertThat(progress.totalSteps()).isEqualTo(2);
        assertThat(progress.completedSteps()).isEqualTo(1);
        assertThat(progress.status()).isEqualTo(TaskStatus.IN_PROGRESS);
    }

    @Test
    void completeStepCompletesTaskWhenFinalStep() {
        Role role = Role.builder().id(2L).name("DEV").build();
        User assigned = User.builder().id(10L).roles(Set.of(role)).build();
        WorkflowStep wfStep = WorkflowStep.builder()
                .name("Only")
                .sequenceOrder(1)
                .requiredRole(role)
                .build();
        TaskStep step = TaskStep.builder()
                .id(2L)
                .assignedUser(assigned)
                .status(TaskStepStatus.ASSIGNED)
                .workflowStep(wfStep)
                .build();
        Task task = Task.builder()
                .id(1L)
                .status(TaskStatus.NOT_STARTED)
                .currentStepIndex(0)
                .taskSteps(new ArrayList<>(List.of(step)))
                .tenant(Tenant.builder().id(5L).name("t1").build())
                .build();
        step.setTask(task);

        when(taskRepository.findByIdAndTenant_Id(1L, 5L)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);

        taskService.completeStep(1L, 2L, 10L);

        assertThat(task.getStatus()).isEqualTo(TaskStatus.COMPLETED);
        assertThat(task.getCompletedAt()).isNotNull();
        assertThat(task.getCurrentStepIndex()).isEqualTo(1);
    }

    @Test
    void completeStepKeepsInProgressStatusWhenAlreadyStarted() {
        Role role = Role.builder().id(2L).name("DEV").build();
        User assigned = User.builder().id(10L).roles(Set.of(role)).build();
        WorkflowStep wfStep1 = WorkflowStep.builder()
                .name("First")
                .sequenceOrder(1)
                .requiredRole(role)
                .build();
        WorkflowStep wfStep2 = WorkflowStep.builder()
                .name("Second")
                .sequenceOrder(2)
                .requiredRole(role)
                .build();
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
                .status(TaskStatus.IN_PROGRESS)
                .currentStepIndex(0)
                .taskSteps(new ArrayList<>(List.of(step1, step2)))
                .tenant(Tenant.builder().id(5L).name("t1").build())
                .build();
        step1.setTask(task);
        step2.setTask(task);

        when(taskRepository.findByIdAndTenant_Id(1L, 5L)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);
        when(priorityService.calculatePriority(any(TaskStep.class))).thenReturn(Priority.MEDIUM_TERM);

        taskService.completeStep(1L, 2L, 10L);

        assertThat(task.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(task.getCurrentStepIndex()).isEqualTo(1);
    }
}
