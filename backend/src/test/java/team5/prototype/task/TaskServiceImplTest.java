package team5.prototype.task;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team5.prototype.role.Role;
import team5.prototype.taskstep.Priority;
import team5.prototype.taskstep.TaskStep;
import team5.prototype.taskstep.TaskStepStatus;
import team5.prototype.tenant.Tenant;
import team5.prototype.user.User;
import team5.prototype.user.UserRepository;
import team5.prototype.workflow.definition.WorkflowDefinition;
import team5.prototype.workflow.definition.WorkflowDefinitionRepository;
import team5.prototype.workflow.step.WorkflowStep;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

    @InjectMocks
    private TaskServiceImpl taskService;

    private WorkflowDefinition definition;
    private WorkflowStep step1;
    private WorkflowStep step2;
    private User creator;
    private User assignee;

    @BeforeEach
    void setUp() {
        Role role = Role.builder().id(1L).name("TEST_ROLE").build();
        step1 = WorkflowStep.builder()
                .id(101L)
                .name("First")
                .sequenceOrder(1)
                .durationHours(2)
                .requiredRole(role)
                .build();
        step2 = WorkflowStep.builder()
                .id(102L)
                .name("Second")
                .sequenceOrder(2)
                .durationHours(3)
                .requiredRole(role)
                .build();

        Tenant tenant = Tenant.builder().id(5L).name("t1").subdomain("t1").build();
        definition = WorkflowDefinition.builder()
                .id(201L)
                .name("WF")
                .tenant(tenant)
                .steps(List.of(step2, step1)) // deliberately unsorted to test ordering
                .build();

        creator = User.builder().id(11L).tenant(tenant).build();
        assignee = User.builder().id(12L).tenant(tenant).build();
    }

    @Test
    void createTaskFromDefinition_buildsOrderedStepsAndSetsPriority() {
        TaskCreationRequest request = new TaskCreationRequest(definition.getId(), "title", "desc",
                LocalDateTime.now().plusDays(1), creator.getId(), Map.of());

        when(definitionRepository.findById(definition.getId())).thenReturn(Optional.of(definition));
        when(userRepository.findById(creator.getId())).thenReturn(Optional.of(creator));
        when(userRepository.findFirstByRoles_NameAndTenant_IdOrderByIdAsc(eq("TEST_ROLE"), eq(definition.getTenant().getId())))
                .thenReturn(Optional.of(assignee));
        when(priorityService.calculatePriority(any(Task.class))).thenReturn(Priority.MEDIUM_TERM);
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Task result = taskService.createTaskFromDefinition(request);

        assertThat(result.getTaskSteps()).hasSize(2);
        TaskStep first = result.getTaskSteps().get(0);
        TaskStep second = result.getTaskSteps().get(1);
        assertThat(first.getWorkflowStep().getId()).isEqualTo(step1.getId());
        assertThat(first.getStatus()).isEqualTo(TaskStepStatus.ASSIGNED);
        assertThat(first.getAssignedUser()).isEqualTo(assignee);
        assertThat(first.getAssignedAt()).isNotNull();
        assertThat(first.getPriority()).isEqualTo(Priority.MEDIUM_TERM);

        assertThat(second.getWorkflowStep().getId()).isEqualTo(step2.getId());
        assertThat(second.getStatus()).isEqualTo(TaskStepStatus.WAITING);
        assertThat(result.getStatus()).isEqualTo(TaskStatus.NOT_STARTED);
        assertThat(result.getCurrentStepIndex()).isEqualTo(0);
    }

    @Test
    void completeStep_advancesToNextAndUpdatesTask() {
        Task task = Task.builder()
                .id(300L)
                .title("t")
                .deadline(LocalDateTime.now().plusDays(1))
                .status(TaskStatus.NOT_STARTED)
                .currentStepIndex(0)
                .build();
        TaskStep s1 = TaskStep.builder()
                .id(1L)
                .task(task)
                .workflowStep(step1)
                .assignedUser(assignee)
                .status(TaskStepStatus.ASSIGNED)
                .assignedAt(LocalDateTime.now())
                .priority(Priority.MEDIUM_TERM)
                .build();
        TaskStep s2 = TaskStep.builder()
                .id(2L)
                .task(task)
                .workflowStep(step2)
                .assignedUser(assignee)
                .status(TaskStepStatus.WAITING)
                .priority(Priority.MEDIUM_TERM)
                .build();
        task.setTaskSteps(List.of(s1, s2));

        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(priorityService.calculatePriority(any(Task.class))).thenReturn(Priority.MEDIUM_TERM);

        taskService.completeStep(task.getId(), s1.getId(), assignee.getId());

        assertThat(s1.getStatus()).isEqualTo(TaskStepStatus.COMPLETED);
        assertThat(s2.getStatus()).isEqualTo(TaskStepStatus.ASSIGNED);
        assertThat(s2.getAssignedAt()).isNotNull();
        assertThat(task.getCurrentStepIndex()).isEqualTo(1);
        assertThat(task.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);

        ArgumentCaptor<Task> saved = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(saved.capture());
        assertThat(saved.getValue().getTaskSteps()).hasSize(2);
    }

    @Test
    void completeStepThrowsWhenUserMismatch() {
        Task task = Task.builder()
                .id(300L)
                .title("t")
                .deadline(LocalDateTime.now().plusDays(1))
                .status(TaskStatus.NOT_STARTED)
                .currentStepIndex(0)
                .build();
        TaskStep s1 = TaskStep.builder()
                .id(1L)
                .task(task)
                .workflowStep(step1)
                .assignedUser(assignee)
                .status(TaskStepStatus.ASSIGNED)
                .assignedAt(LocalDateTime.now())
                .priority(Priority.MEDIUM_TERM)
                .build();
        task.setTaskSteps(List.of(s1));

        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> taskService.completeStep(task.getId(), s1.getId(), 999L))
                .isInstanceOf(IllegalArgumentException.class);
        verify(taskRepository, never()).save(any());
    }

    @Test
    void createTaskThrowsWhenDefinitionMissing() {
        TaskCreationRequest request = new TaskCreationRequest(999L, "t", "d",
                LocalDateTime.now(), creator.getId(), Map.of());

        when(definitionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.createTaskFromDefinition(request))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
