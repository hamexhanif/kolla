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

    @EntityGraph(attributePaths = {"taskSteps", "taskSteps.workflowStep"})
    List<Task> findAllByTenant_Id(Long tenantId);

    @EntityGraph(attributePaths = {"taskSteps", "taskSteps.workflowStep"})
    Optional<Task> findByIdAndTenant_Id(Long id, Long tenantId);
    // TODO: write needed data access function related with Task
}
