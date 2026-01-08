package team5.prototype.taskstep;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskStepRepository extends JpaRepository<TaskStep, Long> {

    List<TaskStep> findByAssignedUserIdAndStatusNot(Long userId, TaskStepStatus status);

    List<TaskStep> findAllByAssignedUserId(Long userId);

    List<TaskStep> findByAssignedUserIdAndStatusNotAndTask_Tenant_Id(Long userId, TaskStepStatus status, Long tenantId);

    Optional<TaskStep> findByIdAndTask_Tenant_Id(Long id, Long tenantId);
}
