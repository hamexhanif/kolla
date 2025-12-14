package team5.prototype.taskstep;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskStepRepository extends JpaRepository<TaskStep, Long> {
    // TODO: write needed data access function related with TaskStep
}
