package team5.prototype.workflow.definition;

import java.util.List;
import java.util.Optional;

public interface WorkflowDefinitionService {
    WorkflowDefinition createWorkflowDefinition(WorkflowDefinition definition);
    List<WorkflowDefinition> getAllDefinitions();
    Optional<WorkflowDefinition> getDefinitionById(Long id);
    void deleteWorkflowDefinition(Long id);
}
