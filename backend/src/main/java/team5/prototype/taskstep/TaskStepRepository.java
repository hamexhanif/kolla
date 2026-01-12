package team5.prototype.taskstep;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskStepRepository extends JpaRepository<TaskStep, Long> {

    List<TaskStep> findByAssignedUserIdAndStatusNot(Long userId, TaskStepStatus status);

    List<TaskStep> findAllByAssignedUserId(Long userId);

    /**
     * Find all non-completed task steps assigned to a user.
     */
    @Query("SELECT ts FROM TaskStep ts " +
            "WHERE ts.assignedUser.id = :userId " +
            "AND ts.status != team5.prototype.taskstep.TaskStepStatus.COMPLETED " +
            "ORDER BY ts.task.deadline ASC")
    List<TaskStep> findActiveTaskStepsByUser(@Param("userId") Long userId);
}
