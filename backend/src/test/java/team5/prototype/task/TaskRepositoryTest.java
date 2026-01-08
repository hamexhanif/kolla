package team5.prototype.task;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import team5.prototype.role.Role;
import team5.prototype.taskstep.Priority;
import team5.prototype.taskstep.TaskStep;
import team5.prototype.taskstep.TaskStepStatus;
import team5.prototype.tenant.Tenant;
import team5.prototype.user.User;
import team5.prototype.workflow.definition.WorkflowDefinition;
import team5.prototype.workflow.step.WorkflowStep;

import jakarta.persistence.PersistenceUnitUtil;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    @Test
    void findAllLoadsTaskStepsViaEntityGraph() {
        Tenant tenant = persistTenant("taskrepo1");
        Role role = persistRole("REVIEWER", tenant);
        User user = persistUser("alex", tenant);
        WorkflowDefinition definition = persistDefinition("WF", tenant);
        WorkflowStep stepTemplate = persistWorkflowStep("Review", role, definition, 1, 2);

        Task task = Task.builder()
                .title("Task A")
                .deadline(LocalDateTime.now().plusDays(1))
                .status(TaskStatus.NOT_STARTED)
                .tenant(tenant)
                .workflowDefinition(definition)
                .createdBy(user)
                .build();
        entityManager.persist(task);

        TaskStep step = TaskStep.builder()
                .task(task)
                .workflowStep(stepTemplate)
                .assignedUser(user)
                .status(TaskStepStatus.ASSIGNED)
                .priority(Priority.MEDIUM_TERM)
                .build();
        entityManager.persist(step);
        entityManager.flush();
        entityManager.clear();

        List<Task> tasks = taskRepository.findAll();

        Task loaded = tasks.stream()
                .filter(candidate -> "Task A".equals(candidate.getTitle()))
                .findFirst()
                .orElseThrow();
        PersistenceUnitUtil util = entityManager.getEntityManagerFactory().getPersistenceUnitUtil();
        assertThat(util.isLoaded(loaded, "taskSteps")).isTrue();
        assertThat(loaded.getTaskSteps()).hasSize(1);
        assertThat(loaded.getTaskSteps().get(0).getWorkflowStep().getName()).isEqualTo("Review");
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
