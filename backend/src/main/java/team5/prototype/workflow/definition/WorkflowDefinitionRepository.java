package team5.prototype.workflow.definition;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkflowDefinitionRepository extends JpaRepository<WorkflowDefinition, Long> {
    // Vorerst leer.
    // TODO: write needed data access function related with WorkflowDefinition

    List<WorkflowDefinition> findAllByTenantId(Long tenantId);
    Optional<WorkflowDefinition> findByIdAndTenantId(Long id, Long tenantId);
}
