package team5.prototype.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import team5.prototype.workflow.definition.WorkflowDefinition;
import team5.prototype.workflow.definition.WorkflowDefinitionRepository;
import team5.prototype.workflow.step.WorkflowStep;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class ValidTaskDeadlineValidatorTest {

    private WorkflowDefinitionRepository definitionRepository;
    private ValidTaskDeadlineValidator validator;

    @BeforeEach
    void setUp() {
        definitionRepository = Mockito.mock(WorkflowDefinitionRepository.class);
        validator = new ValidTaskDeadlineValidator();
        ReflectionTestUtils.setField(validator, "workflowDefinitionRepository", definitionRepository);
    }

    @Test
    void isValidReturnsTrueWhenDeadlineMissing() {
        TaskDto dto = new TaskDto();
        dto.setWorkflowDefinitionId(1L);

        assertThat(validator.isValid(dto, null)).isTrue();
    }

    @Test
    void isValidReturnsTrueWhenWorkflowDefinitionMissing() {
        TaskDto dto = new TaskDto();
        dto.setDeadline(LocalDateTime.now().plusHours(1));

        assertThat(validator.isValid(dto, null)).isTrue();
    }

    @Test
    void isValidReturnsFalseWhenDefinitionNotFound() {
        TaskDto dto = new TaskDto();
        dto.setWorkflowDefinitionId(99L);
        dto.setDeadline(LocalDateTime.now().plusHours(2));

        when(definitionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(validator.isValid(dto, null)).isFalse();
    }

    @Test
    void isValidReturnsFalseWhenDeadlineTooEarly() {
        WorkflowDefinition definition = WorkflowDefinition.builder()
                .steps(List.of(
                        WorkflowStep.builder().durationHours(5).build()
                ))
                .build();
        TaskDto dto = new TaskDto();
        dto.setWorkflowDefinitionId(1L);
        dto.setDeadline(LocalDateTime.now().plusHours(1));

        when(definitionRepository.findById(1L)).thenReturn(Optional.of(definition));

        assertThat(validator.isValid(dto, null)).isFalse();
    }

    @Test
    void isValidReturnsTrueWhenDeadlineSufficient() {
        WorkflowDefinition definition = WorkflowDefinition.builder()
                .steps(List.of(
                        WorkflowStep.builder().durationHours(2).build()
                ))
                .build();
        TaskDto dto = new TaskDto();
        dto.setWorkflowDefinitionId(1L);
        dto.setDeadline(LocalDateTime.now().plusHours(10));

        when(definitionRepository.findById(1L)).thenReturn(Optional.of(definition));

        assertThat(validator.isValid(dto, null)).isTrue();
    }
}
