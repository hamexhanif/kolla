// VOLLSTAENDIGER, KORRIGIERTER INHALT FUER TaskServiceImplTest.java

package team5.prototype.task;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team5.prototype.notification.NotificationService;
import team5.prototype.role.Role;
import team5.prototype.taskstep.PriorityService;
import team5.prototype.taskstep.TaskStep;
import team5.prototype.taskstep.TaskStepDto;
import team5.prototype.taskstep.TaskStepRepository;
import team5.prototype.taskstep.TaskStepStatus;
import team5.prototype.taskstep.Priority;
import team5.prototype.dto.TaskDetailsDto;
import team5.prototype.tenant.Tenant;
import team5.prototype.user.User;
import team5.prototype.user.UserRepository;
import team5.prototype.workflow.definition.WorkflowDefinition;
import team5.prototype.workflow.definition.WorkflowDefinitionRepository;
import team5.prototype.workflow.step.WorkflowStep;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

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

    @InjectMocks
    private TaskServiceImpl taskService;

    private WorkflowDefinition definition;
    private User creator;
    private Tenant tenant;
    private Role role;

    @BeforeEach
    void setUp() {
        role = Role.builder().id(1L).name("TEST_ROLE").build();
        tenant = Tenant.builder().id(5L).name("t1").build();
        definition = WorkflowDefinition.builder().id(201L).name("WF").tenant(tenant).steps(new ArrayList<>()).build();
        creator = User.builder().id(11L).tenant(tenant).roles(Set.of(role)).build();
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

        when(definitionRepository.findById(999L)).thenReturn(Optional.empty());

        // Die Assertion bleibt gleich, aber ruft jetzt die korrekte Methode auf.
        assertThatThrownBy(() -> taskService.createTaskFromDefinition(requestDto))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void createTaskThrowsWhenNoStepsConfigured() {
        TaskDto requestDto = new TaskDto();
        requestDto.setWorkflowDefinitionId(definition.getId());
        requestDto.setTitle("t");
        requestDto.setDescription("d");
        requestDto.setDeadline(LocalDateTime.now().plusHours(2));
        requestDto.setCreatedById(creator.getId());

        when(definitionRepository.findById(definition.getId())).thenReturn(Optional.of(definition));

        assertThatThrownBy(() -> taskService.createTaskFromDefinition(requestDto))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void createTaskThrowsWhenDeadlineTooEarly() {
        WorkflowStep step = WorkflowStep.builder()
                .id(101L)
                .name("First")
                .sequenceOrder(1)
                .durationHours(5)
                .requiredRole(role)
                .build();
        definition.setSteps(List.of(step));

        TaskDto requestDto = new TaskDto();
        requestDto.setWorkflowDefinitionId(definition.getId());
        requestDto.setTitle("t");
        requestDto.setDescription("d");
        requestDto.setDeadline(LocalDateTime.now().plusHours(3));
        requestDto.setCreatedById(creator.getId());

        when(definitionRepository.findById(definition.getId())).thenReturn(Optional.of(definition));

        assertThatThrownBy(() -> taskService.createTaskFromDefinition(requestDto))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void createTaskBuildsStepsAndSaves() {
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
                .durationHours(1)
                .requiredRole(role)
                .build();
        definition.setSteps(List.of(step1, step2));

        User assignee = User.builder().id(12L).tenant(tenant).build();

        TaskDto requestDto = new TaskDto();
        requestDto.setWorkflowDefinitionId(definition.getId());
        requestDto.setTitle("t");
        requestDto.setDescription("d");
        requestDto.setDeadline(LocalDateTime.now().plusHours(5));
        requestDto.setCreatedById(creator.getId());
        requestDto.setStepAssignments(Map.of(step1.getId(), assignee.getId()));

        when(definitionRepository.findById(definition.getId())).thenReturn(Optional.of(definition));
        when(userRepository.findById(creator.getId())).thenReturn(Optional.of(creator));
        when(userRepository.findById(assignee.getId())).thenReturn(Optional.of(assignee));
        when(userRepository.findActiveUsersByRoleAndTenant(role.getName(), tenant.getId()))
                .thenReturn(List.of(assignee));
        when(taskStepRepository.findActiveTaskStepsByUser(assignee.getId())).thenReturn(List.of());
        when(priorityService.calculatePriority(any(TaskStep.class))).thenReturn(Priority.MEDIUM_TERM);
        when(priorityService.calculatePriority(any(Task.class))).thenReturn(Priority.MEDIUM_TERM);
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskDto created = taskService.createTaskFromDefinition(requestDto);

        assertThat(created.getSteps()).hasSize(2);
        TaskStepDto first = created.getSteps().get(0);
        TaskStepDto second = created.getSteps().get(1);
        assertThat(first.getStatus()).isEqualTo(TaskStepStatus.ASSIGNED.name());
        assertThat(first.getPriority()).isEqualTo(Priority.MEDIUM_TERM.name());
        assertThat(second.getStatus()).isEqualTo(TaskStepStatus.WAITING.name());
        assertThat(second.getPriority()).isEqualTo(Priority.MEDIUM_TERM.name());
    }

    @Test
    void completeStepThrowsWhenUserMismatch() {
        Task task = buildTaskWithSingleStep(TaskStepStatus.ASSIGNED, creator, role);
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> taskService.completeStep(task.getId(), task.getTaskSteps().get(0).getId(), 999L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void completeStepThrowsWhenUserMissingRole() {
        Role required = Role.builder().id(44L).name("REQ").build();
        User assigned = User.builder().id(55L).roles(Set.of()).build();
        WorkflowStep wf = WorkflowStep.builder().id(2L).name("Step").sequenceOrder(1).requiredRole(required).build();
        Task task = Task.builder()
                .id(100L)
                .title("t")
                .deadline(LocalDateTime.now().plusDays(1))
                .workflowDefinition(definition)
                .tenant(tenant)
                .status(TaskStatus.IN_PROGRESS)
                .taskSteps(new ArrayList<>())
                .build();
        TaskStep step = TaskStep.builder()
                .id(9L)
                .task(task)
                .workflowStep(wf)
                .assignedUser(assigned)
                .status(TaskStepStatus.ASSIGNED)
                .build();
        task.getTaskSteps().add(step);

        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> taskService.completeStep(task.getId(), step.getId(), assigned.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void completeStepReturnsWhenAlreadyCompleted() {
        Task task = buildTaskWithSingleStep(TaskStepStatus.COMPLETED, creator, role);
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

        taskService.completeStep(task.getId(), task.getTaskSteps().get(0).getId(), creator.getId());

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void completeStepAdvancesToNextStepAndNotifies() {
        User assignee = User.builder().id(12L).roles(Set.of(role)).build();
        WorkflowStep wf1 = WorkflowStep.builder().id(11L).name("First").sequenceOrder(1).requiredRole(role).build();
        WorkflowStep wf2 = WorkflowStep.builder().id(12L).name("Second").sequenceOrder(2).requiredRole(role).build();
        Task task = Task.builder()
                .id(200L)
                .title("t")
                .deadline(LocalDateTime.now().plusDays(1))
                .workflowDefinition(definition)
                .tenant(tenant)
                .status(TaskStatus.IN_PROGRESS)
                .taskSteps(new ArrayList<>())
                .build();
        TaskStep step1 = TaskStep.builder()
                .id(1L)
                .task(task)
                .workflowStep(wf1)
                .assignedUser(assignee)
                .status(TaskStepStatus.ASSIGNED)
                .assignedAt(LocalDateTime.now().minusHours(1))
                .build();
        TaskStep step2 = TaskStep.builder()
                .id(2L)
                .task(task)
                .workflowStep(wf2)
                .assignedUser(assignee)
                .status(TaskStepStatus.WAITING)
                .build();
        task.getTaskSteps().add(step1);
        task.getTaskSteps().add(step2);

        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(priorityService.calculatePriority(any(TaskStep.class))).thenReturn(Priority.IMMEDIATE);

        taskService.completeStep(task.getId(), step1.getId(), assignee.getId());

        assertThat(step1.getStatus()).isEqualTo(TaskStepStatus.COMPLETED);
        assertThat(step2.getStatus()).isEqualTo(TaskStepStatus.ASSIGNED);
        verify(taskRepository).save(task);
        verify(notificationService).sendTaskUpdateNotification(
                org.mockito.ArgumentMatchers.eq(task.getId()),
                any(Map.class)
        );
    }

    @Test
    void getTaskProgressCountsCompletedSteps() {
        Task task = buildTaskWithSteps(TaskStepStatus.COMPLETED, TaskStepStatus.ASSIGNED);
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

        TaskProgress progress = taskService.getTaskProgress(task.getId());

        assertThat(progress.completedSteps()).isEqualTo(1);
        assertThat(progress.totalSteps()).isEqualTo(2);
    }

    @Test
    void getTaskDetailsBuildsSortedStepDetails() {
        User assignee = User.builder().id(12L).username("u").firstName("A").lastName("B").build();
        WorkflowStep wf1 = WorkflowStep.builder().id(1L).name("Alpha").sequenceOrder(2).durationHours(4).requiredRole(role).build();
        WorkflowStep wf2 = WorkflowStep.builder().id(2L).name("Beta").sequenceOrder(1).durationHours(1).requiredRole(role).build();
        WorkflowDefinition wfDef = WorkflowDefinition.builder()
                .id(300L)
                .name("WF")
                .tenant(tenant)
                .steps(List.of(wf1, wf2))
                .build();
        Task task = Task.builder()
                .id(400L)
                .title("Task")
                .deadline(LocalDateTime.now().plusHours(10))
                .workflowDefinition(wfDef)
                .tenant(tenant)
                .status(TaskStatus.IN_PROGRESS)
                .taskSteps(new ArrayList<>())
                .build();
        TaskStep step1 = TaskStep.builder()
                .id(10L)
                .task(task)
                .workflowStep(wf1)
                .assignedUser(assignee)
                .status(TaskStepStatus.ASSIGNED)
                .build();
        TaskStep step2 = TaskStep.builder()
                .id(11L)
                .task(task)
                .workflowStep(wf2)
                .assignedUser(assignee)
                .status(TaskStepStatus.COMPLETED)
                .build();
        task.getTaskSteps().add(step1);
        task.getTaskSteps().add(step2);

        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(priorityService.calculatePriority(any(TaskStep.class))).thenReturn(Priority.IMMEDIATE);
        when(priorityService.calculatePriority(any(Task.class))).thenReturn(Priority.IMMEDIATE);

        TaskDetailsDto details = taskService.getTaskDetails(task.getId());

        assertThat(details.totalSteps()).isEqualTo(2);
        assertThat(details.completedSteps()).isEqualTo(1);
        assertThat(details.steps()).hasSize(2);
        assertThat(details.steps().get(0).stepName()).isEqualTo("Beta");
        assertThat(details.steps().get(0).assigneeName()).isEqualTo("A B");
    }

    @Test
    void getManagerDashboardAggregatesCounts() {
        LocalDateTime now = LocalDateTime.now();
        Task openTask = Task.builder()
                .id(1L)
                .title("Open")
                .deadline(now.minusDays(1))
                .status(TaskStatus.IN_PROGRESS)
                .taskSteps(List.of())
                .build();
        Task dueToday = Task.builder()
                .id(2L)
                .title("Today")
                .deadline(now)
                .status(TaskStatus.IN_PROGRESS)
                .taskSteps(List.of())
                .build();
        Task completed = Task.builder()
                .id(3L)
                .title("Done")
                .deadline(now.minusDays(2))
                .status(TaskStatus.COMPLETED)
                .taskSteps(List.of())
                .build();

        when(taskRepository.findAll()).thenReturn(List.of(openTask, dueToday, completed));

        var dashboard = taskService.getManagerDashboard();

        assertThat(dashboard.openTasks()).isEqualTo(2);
        assertThat(dashboard.overdueTasks()).isEqualTo(1);
        assertThat(dashboard.dueTodayTasks()).isEqualTo(1);
        assertThat(dashboard.tasks()).hasSize(3);
    }

    @Test
    void getAllTasksAsDtoMapsSteps() {
        TaskStep step = TaskStep.builder().id(5L).status(TaskStepStatus.ASSIGNED).priority(Priority.IMMEDIATE).build();
        Task task = Task.builder()
                .id(10L)
                .title("Task")
                .deadline(LocalDateTime.now().plusDays(1))
                .status(TaskStatus.IN_PROGRESS)
                .taskSteps(List.of(step))
                .build();

        when(taskRepository.findAll()).thenReturn(List.of(task));

        List<TaskDto> result = taskService.getAllTasksAsDto();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSteps()).hasSize(1);
        assertThat(result.get(0).getSteps().get(0).getId()).isEqualTo(5L);
    }

    private Task buildTaskWithSingleStep(TaskStepStatus status, User assignedUser, Role requiredRole) {
        WorkflowStep wf = WorkflowStep.builder().id(1L).name("Step").sequenceOrder(1).requiredRole(requiredRole).build();
        Task task = Task.builder()
                .id(500L)
                .title("t")
                .deadline(LocalDateTime.now().plusDays(1))
                .workflowDefinition(definition)
                .tenant(tenant)
                .status(TaskStatus.IN_PROGRESS)
                .taskSteps(new ArrayList<>())
                .build();
        TaskStep step = TaskStep.builder()
                .id(600L)
                .task(task)
                .workflowStep(wf)
                .assignedUser(assignedUser)
                .status(status)
                .build();
        task.getTaskSteps().add(step);
        return task;
    }

    private Task buildTaskWithSteps(TaskStepStatus firstStatus, TaskStepStatus secondStatus) {
        WorkflowStep wf1 = WorkflowStep.builder().id(1L).name("A").sequenceOrder(1).requiredRole(role).build();
        WorkflowStep wf2 = WorkflowStep.builder().id(2L).name("B").sequenceOrder(2).requiredRole(role).build();
        Task task = Task.builder()
                .id(700L)
                .title("t")
                .deadline(LocalDateTime.now().plusDays(1))
                .workflowDefinition(definition)
                .tenant(tenant)
                .status(TaskStatus.IN_PROGRESS)
                .taskSteps(new ArrayList<>())
                .build();
        task.getTaskSteps().add(TaskStep.builder().id(1L).task(task).workflowStep(wf1).status(firstStatus).build());
        task.getTaskSteps().add(TaskStep.builder().id(2L).task(task).workflowStep(wf2).status(secondStatus).build());
        return task;
    }
}
