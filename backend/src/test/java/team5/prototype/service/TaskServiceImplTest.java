package team5.prototype.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import team5.prototype.entity.*;
import team5.prototype.repository.TaskRepository;
import team5.prototype.repository.UserRepository;
import team5.prototype.repository.WorkflowDefinitionRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TaskServiceImplTest {

    private final TaskRepository taskRepository = mock(TaskRepository.class);
    private final WorkflowDefinitionRepository definitionRepository = mock(WorkflowDefinitionRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final PriorityService priorityService = mock(PriorityService.class);
    private final Clock clock = Clock.fixed(Instant.parse("2025-01-01T00:00:00Z"), ZoneOffset.UTC);

    private TaskServiceImpl taskService;

    @BeforeEach
    void setUp() {
        taskService = new TaskServiceImpl(taskRepository, definitionRepository, userRepository, priorityService, clock);
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createTaskFromDefinitionCopiesWorkflowAndAssignsUsers() {
        WorkflowDefinition definition = workflowDefinition();
        when(definitionRepository.findById(definition.getId())).thenReturn(Optional.of(definition));

        User creator = userWithId(42L);
        when(userRepository.findById(creator.getId())).thenReturn(Optional.of(creator));

        User developer = userWithId(100L);
        when(userRepository.findFirstByRoles_NameAndTenant_IdOrderByIdAsc("Developer", definition.getTenant().getId()))
                .thenReturn(Optional.of(developer));

        User tester = userWithId(200L);
        when(userRepository.findFirstByRoles_NameAndTenant_IdOrderByIdAsc("Tester", definition.getTenant().getId()))
                .thenReturn(Optional.of(tester));

        when(priorityService.calculatePriority(any(Task.class))).thenReturn(Priority.MEDIUM_TERM);

        TaskCreationRequest request = new TaskCreationRequest(
                definition.getId(),
                "Create Feature",
                "Implement feature X",
                LocalDateTime.ofInstant(clock.instant(), clock.getZone()).plusDays(2),
                creator.getId(),
                null
        );

        Task created = taskService.createTaskFromDefinition(request);

        assertThat(created.getTaskSteps()).hasSize(2);
        TaskStep firstStep = created.getTaskSteps().get(0);
        assertThat(firstStep.getAssignedUser().getId()).isEqualTo(developer.getId());
        assertThat(firstStep.getStatus()).isEqualTo(TaskStepStatus.ASSIGNED);
        assertThat(firstStep.getAssignedAt()).isNotNull();
        assertThat(firstStep.getPriority()).isEqualTo(Priority.MEDIUM_TERM);

        TaskStep secondStep = created.getTaskSteps().get(1);
        assertThat(secondStep.getAssignedUser().getId()).isEqualTo(tester.getId());
        assertThat(secondStep.getStatus()).isEqualTo(TaskStepStatus.WAITING);
    }

    @Test
    void completeStepAdvancesWorkflowAndUpdatesPriority() {
        Task task = buildPersistedTask();
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(priorityService.calculatePriority(task)).thenReturn(Priority.IMMEDIATE);

        taskService.completeStep(task.getId(), 10L, 100L);

        TaskStep completed = task.getTaskSteps().get(0);
        assertThat(completed.getStatus()).isEqualTo(TaskStepStatus.COMPLETED);
        assertThat(completed.getCompletedAt()).isNotNull();

        TaskStep next = task.getTaskSteps().get(1);
        assertThat(next.getStatus()).isEqualTo(TaskStepStatus.ASSIGNED);
        assertThat(next.getAssignedAt()).isNotNull();
        assertThat(next.getPriority()).isEqualTo(Priority.IMMEDIATE);

        verify(taskRepository).save(task);
    }

    @Test
    void getTaskProgressSummarizesTask() {
        Task task = buildPersistedTask();
        task.getTaskSteps().get(0).setStatus(TaskStepStatus.COMPLETED);
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

        TaskProgress progress = taskService.getTaskProgress(task.getId());
        assertThat(progress.taskId()).isEqualTo(task.getId());
        assertThat(progress.totalSteps()).isEqualTo(2);
        assertThat(progress.completedSteps()).isEqualTo(1);
    }

    @Test
    void throwsWhenUnknownStepCompleted() {
        Task task = buildPersistedTask();
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> taskService.completeStep(task.getId(), 999L, 100L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    private WorkflowDefinition workflowDefinition() {
        Tenant tenant = Tenant.builder().id(5L).build();
        WorkflowStep step1 = WorkflowStep.builder()
                .id(1L)
                .name("Code")
                .sequenceOrder(0)
                .durationHours(8)
                .requiredRole(Role.builder().name("Developer").tenant(tenant).build())
                .build();
        WorkflowStep step2 = WorkflowStep.builder()
                .id(2L)
                .name("Test")
                .sequenceOrder(1)
                .durationHours(4)
                .requiredRole(Role.builder().name("Tester").tenant(tenant).build())
                .build();
        return WorkflowDefinition.builder()
                .id(7L)
                .tenant(tenant)
                .steps(List.of(step1, step2))
                .build();
    }

    private User userWithId(Long id) {
        return User.builder().id(id).build();
    }

    private Task buildPersistedTask() {
        Task task = new Task();
        task.setId(55L);
        task.setStatus(TaskStatus.NOT_STARTED);
        task.setCurrentStepIndex(0);

        TaskStep step1 = TaskStep.builder()
                .id(10L)
                .task(task)
                .workflowStep(WorkflowStep.builder().sequenceOrder(0).durationHours(4).requiredRole(Role.builder().name("Dev").build()).build())
                .assignedUser(userWithId(100L))
                .status(TaskStepStatus.ASSIGNED)
                .assignedAt(LocalDateTime.ofInstant(clock.instant(), clock.getZone()))
                .build();

        TaskStep step2 = TaskStep.builder()
                .id(11L)
                .task(task)
                .workflowStep(WorkflowStep.builder().sequenceOrder(1).durationHours(4).requiredRole(Role.builder().name("QA").build()).build())
                .assignedUser(userWithId(200L))
                .status(TaskStepStatus.WAITING)
                .build();

        task.setTaskSteps(List.of(step1, step2));
        task.setDeadline(LocalDateTime.ofInstant(clock.instant(), clock.getZone()).plusDays(1));
        return task;
    }
}
