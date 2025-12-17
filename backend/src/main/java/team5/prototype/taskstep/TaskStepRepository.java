package team5.prototype.taskstep;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskStepRepository extends JpaRepository<TaskStep, Long> {

    List<TaskStep> findByAssignedUserIdAndStatusNot(Long userId, TaskStepStatus status);
}
