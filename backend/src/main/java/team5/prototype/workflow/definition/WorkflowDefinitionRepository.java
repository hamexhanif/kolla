package team5.prototype.workflow.definition;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkflowDefinitionRepository extends JpaRepository<WorkflowDefinition, Long> {
    // Vorerst leer.
    // TODO: write needed data access function related with WorkflowDefinition
}