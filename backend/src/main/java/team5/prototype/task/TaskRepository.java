package team5.prototype.task;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    @Override
    @EntityGraph(attributePaths = {"taskSteps", "taskSteps.workflowStep"})
    List<Task> findAll();
    // TODO: write needed data access function related with Task
}