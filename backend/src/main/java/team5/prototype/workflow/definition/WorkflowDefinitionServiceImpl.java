package team5.prototype.workflow.definition;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorkflowDefinitionServiceImpl implements WorkflowDefinitionService {

    private final WorkflowDefinitionRepository definitionRepository;

    public WorkflowDefinitionServiceImpl(WorkflowDefinitionRepository definitionRepository) {
        this.definitionRepository = definitionRepository;
    }

    @Override // Diese Annotation ist wichtig
    public List<WorkflowDefinition> getAllDefinitions() {

        return definitionRepository.findAll();
    }

    // --- NEUE IMPLEMENTIERUNGEN ---
    @Override
    public WorkflowDefinition createWorkflowDefinition(WorkflowDefinition definition) {
        // Hier könnten Validierungen hinzukommen
        return definitionRepository.save(definition);
    }

    @Override
    public void deleteWorkflowDefinition(Long definitionId) {
        definitionRepository.deleteById(definitionId);
    }
}

