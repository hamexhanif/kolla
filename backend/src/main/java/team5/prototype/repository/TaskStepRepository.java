package team5.prototype.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import team5.prototype.entity.TaskStep;

@Repository
public interface TaskStepRepository extends JpaRepository<TaskStep, Long> {
}
