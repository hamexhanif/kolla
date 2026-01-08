package team5.prototype.taskstep;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import team5.prototype.role.Role;
import team5.prototype.task.Task;
import team5.prototype.task.TaskStatus;
import team5.prototype.tenant.Tenant;
import team5.prototype.user.User;
import team5.prototype.workflow.definition.WorkflowDefinition;
import team5.prototype.workflow.step.WorkflowStep;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class TaskStepRepositoryTest {

    @Autowired
    private TaskStepRepository taskStepRepository;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    @Test
    void findByAssignedUserIdAndStatusNotFiltersCompleted() {
        Tenant tenant = persistTenant("taskstep1");
        Role role = persistRole("REVIEWER", tenant);
        WorkflowDefinition definition = persistDefinition("WF", tenant);
        WorkflowStep stepTemplate = persistWorkflowStep("Review", role, definition, 1, 2);
        User user = persistUser("alex", tenant);

        Task task = Task.builder()
                .title("Task A")
                .deadline(LocalDateTime.now().plusDays(1))
                .status(TaskStatus.NOT_STARTED)
                .tenant(tenant)
                .workflowDefinition(definition)
                .createdBy(user)
                .build();
        entityManager.persist(task);

        TaskStep active = TaskStep.builder()
                .task(task)
                .workflowStep(stepTemplate)
                .assignedUser(user)
                .status(TaskStepStatus.ASSIGNED)
                .priority(Priority.MEDIUM_TERM)
                .build();
        TaskStep completed = TaskStep.builder()
                .task(task)
                .workflowStep(stepTemplate)
                .assignedUser(user)
                .status(TaskStepStatus.COMPLETED)
                .priority(Priority.MEDIUM_TERM)
                .build();

        entityManager.persist(active);
        entityManager.persist(completed);
        entityManager.flush();

        List<TaskStep> result = taskStepRepository.findByAssignedUserIdAndStatusNot(user.getId(), TaskStepStatus.COMPLETED);

        assertThat(result).containsExactly(active);
    }

    @Test
    void findAllByAssignedUserIdReturnsAllForUser() {
        Tenant tenant = persistTenant("taskstep2");
        Role role = persistRole("AUTHOR", tenant);
        WorkflowDefinition definition = persistDefinition("WF2", tenant);
        WorkflowStep stepTemplate = persistWorkflowStep("Draft", role, definition, 1, 3);
        User user = persistUser("mia", tenant);

        Task task = Task.builder()
                .title("Task B")
                .deadline(LocalDateTime.now().plusDays(2))
                .status(TaskStatus.NOT_STARTED)
                .tenant(tenant)
                .workflowDefinition(definition)
                .createdBy(user)
                .build();
        entityManager.persist(task);

        TaskStep first = TaskStep.builder()
                .task(task)
                .workflowStep(stepTemplate)
                .assignedUser(user)
                .status(TaskStepStatus.ASSIGNED)
                .priority(Priority.MEDIUM_TERM)
                .build();
        TaskStep second = TaskStep.builder()
                .task(task)
                .workflowStep(stepTemplate)
                .assignedUser(user)
                .status(TaskStepStatus.WAITING)
                .priority(Priority.LONG_TERM)
                .build();
        entityManager.persist(first);
        entityManager.persist(second);
        entityManager.flush();

        List<TaskStep> result = taskStepRepository.findAllByAssignedUserId(user.getId());

        assertThat(result).containsExactlyInAnyOrder(first, second);
    }

    private Tenant persistTenant(String suffix) {
        Tenant tenant = Tenant.builder()
                .name("Tenant " + suffix)
                .subdomain("sub-" + suffix)
                .active(true)
                .build();
        entityManager.persist(tenant);
        return tenant;
    }

    private Role persistRole(String name, Tenant tenant) {
        Role role = Role.builder()
                .name(name)
                .tenant(tenant)
                .build();
        entityManager.persist(role);
        return role;
    }

    private WorkflowDefinition persistDefinition(String name, Tenant tenant) {
        WorkflowDefinition definition = WorkflowDefinition.builder()
                .name(name)
                .tenant(tenant)
                .build();
        entityManager.persist(definition);
        return definition;
    }

    private WorkflowStep persistWorkflowStep(String name, Role role, WorkflowDefinition definition, int order, int duration) {
        WorkflowStep step = WorkflowStep.builder()
                .name(name)
                .durationHours(duration)
                .sequenceOrder(order)
                .requiredRole(role)
                .workflowDefinition(definition)
                .build();
        entityManager.persist(step);
        return step;
    }

    private User persistUser(String username, Tenant tenant) {
        User user = User.builder()
                .username(username)
                .email(username + "@example.com")
                .passwordHash("hash")
                .active(true)
                .tenant(tenant)
                .build();
        entityManager.persist(user);
        return user;
    }
}
