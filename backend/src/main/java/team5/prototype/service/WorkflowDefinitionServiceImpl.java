package team5.prototype.service;

import org.springframework.stereotype.Service;
import team5.prototype.entity.WorkflowDefinition;
import team5.prototype.repository.WorkflowDefinitionRepository;

import java.util.List;

@Service
public class WorkflowDefinitionServiceImpl implements WorkflowDefinitionService{
    private final WorkflowDefinitionRepository definitionRepository;

    public WorkflowDefinitionServiceImpl(WorkflowDefinitionRepository definitionRepository) {
        this.definitionRepository = definitionRepository;
    }

    @Override
    public List<WorkflowDefinition> getAllDefinitions() {
        // Ruft einfach die eingebaute findAll()-Methode von JpaRepository auf.
        return definitionRepository.findAll();
    }
}
