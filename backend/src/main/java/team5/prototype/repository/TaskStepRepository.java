package team5.prototype.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import team5.prototype.entity.TaskStep;
import team5.prototype.entity.TaskStepStatus;

import java.util.List;

@Repository
public interface TaskStepRepository extends JpaRepository<TaskStep, Long> {

    List<TaskStep> findByAssignedUserIdAndStatusNot(Long userId, TaskStepStatus status);
}
