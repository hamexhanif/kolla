package team5.prototype.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import team5.prototype.tenant.TenantContext;
import team5.prototype.workflow.definition.WorkflowDefinition;
import team5.prototype.workflow.definition.WorkflowDefinitionRepository;
import team5.prototype.workflow.step.WorkflowStep;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidTaskDeadlineValidatorTest {

    @Mock
    private WorkflowDefinitionRepository workflowDefinitionRepository;

    private ValidTaskDeadlineValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ValidTaskDeadlineValidator();
        ReflectionTestUtils.setField(validator, "workflowDefinitionRepository", workflowDefinitionRepository);
        TenantContext.setTenantId(1L);
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void allowsNullValues() {
        TaskDto taskDto = new TaskDto();

        assertThat(validator.isValid(taskDto, null)).isTrue();
    }

    @Test
    void returnsFalseWhenDefinitionMissing() {
        TaskDto taskDto = new TaskDto();
        taskDto.setWorkflowDefinitionId(99L);
        taskDto.setDeadline(LocalDateTime.now().plusHours(5));

        when(workflowDefinitionRepository.findByIdAndTenantId(99L, 1L)).thenReturn(Optional.empty());

        assertThat(validator.isValid(taskDto, null)).isFalse();
    }

    @Test
    void validatesDeadlineAgainstTotalDuration() {
        WorkflowStep step = WorkflowStep.builder()
                .id(1L)
                .name("Step")
                .durationHours(4)
                .sequenceOrder(1)
                .build();
        WorkflowDefinition definition = WorkflowDefinition.builder()
                .id(10L)
                .name("WF")
                .steps(List.of(step))
                .build();

        TaskDto taskDto = new TaskDto();
        taskDto.setWorkflowDefinitionId(10L);
        taskDto.setDeadline(LocalDateTime.now().plusHours(6));

        when(workflowDefinitionRepository.findByIdAndTenantId(10L, 1L)).thenReturn(Optional.of(definition));

        assertThat(validator.isValid(taskDto, null)).isTrue();
    }
}
