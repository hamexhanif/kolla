package team5.prototype.workflow.definition;

import java.util.List;

public interface WorkflowDefinitionService {

    WorkflowDefinition createWorkflowDefinition(WorkflowDefinition definition);

    WorkflowDefinitionDto getWorkflowDefinitionByIdAsDto(Long id);

    List<WorkflowDefinitionDto> getAllWorkflowDefinitionsAsDto();

    WorkflowDefinitionDto createWorkflowDefinitionAsDto(WorkflowDefinition definition);
}
