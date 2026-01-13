package team5.prototype.task;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    @Override
    @EntityGraph(attributePaths = {"taskSteps", "taskSteps.workflowStep"})
    List<Task> findAll();
    // TODO: write needed data access function related with Task

    @EntityGraph(attributePaths = {"taskSteps", "taskSteps.workflowStep"})
    List<Task> findAllByTenantId(Long tenantId);

    @EntityGraph(attributePaths = {"taskSteps", "taskSteps.workflowStep"})
    Optional<Task> findByIdAndTenantId(Long id, Long tenantId);
}
