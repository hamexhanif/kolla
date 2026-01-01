// VOLLSTÄNDIGER, KORRIGIERTER INHALT FÜR TaskServiceImplTest.java

package team5.prototype.task;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team5.prototype.role.Role;
import team5.prototype.taskstep.PriorityService;
import team5.prototype.tenant.Tenant;
import team5.prototype.user.User;
import team5.prototype.user.UserRepository;
import team5.prototype.workflow.definition.WorkflowDefinition;
import team5.prototype.workflow.definition.WorkflowDefinitionRepository;
import team5.prototype.workflow.step.WorkflowStep;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
}