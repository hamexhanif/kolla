package team5.prototype.workflow.definition;

import java.util.List;

public interface WorkflowDefinitionService {

    List<WorkflowDefinition> getAllDefinitions();

    // --- NEUE METHODEN ---
    WorkflowDefinition createWorkflowDefinition(WorkflowDefinition definition);
    void deleteWorkflowDefinition(Long definitionId);
}
