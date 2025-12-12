package team5.prototype.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import team5.prototype.entity.WorkflowDefinition;

@Repository
public interface WorkflowDefinitionRepository extends JpaRepository<WorkflowDefinition, Long> {
    // Vorerst leer.
}