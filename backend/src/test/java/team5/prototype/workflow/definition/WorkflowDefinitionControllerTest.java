package team5.prototype.workflow.definition;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkflowDefinitionControllerTest {

    @Mock
    private WorkflowDefinitionService workflowDefinitionService;

    @InjectMocks
    private WorkflowDefinitionController controller;

    @Test
    void getWorkflowDefinitionReturnsNotFoundWhenMissing() {
        when(workflowDefinitionService.getWorkflowDefinitionByIdAsDto(10L)).thenReturn(null);

        ResponseEntity<WorkflowDefinitionDto> response = controller.getWorkflowDefinition(10L);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void getAllWorkflowDefinitionsReturnsList() {
        when(workflowDefinitionService.getAllWorkflowDefinitionsAsDto()).thenReturn(List.of(new WorkflowDefinitionDto()));

        ResponseEntity<List<WorkflowDefinitionDto>> response = controller.getAllWorkflowDefinitions();

        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void createWorkflowDefinitionReturnsCreated() {
        WorkflowDefinitionDto dto = new WorkflowDefinitionDto();
        when(workflowDefinitionService.createWorkflowDefinitionAsDto(org.mockito.ArgumentMatchers.any(WorkflowDefinition.class)))
                .thenReturn(dto);

        ResponseEntity<WorkflowDefinitionDto> response = controller.createWorkflowDefinition(new WorkflowDefinition());

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody()).isEqualTo(dto);
    }
}
