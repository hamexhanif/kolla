package team5.prototype.taskstep;

import java.util.List;

public interface TaskStepService {

    void assignTaskStepToUser(Long taskStepId, Long userId);

    void completeTaskStep(Long taskStepId, String userId);

    List<TaskStep> getTaskStepsByUserId(Long userId);

// TODO: write method header for calculatePriority,
//       overridePriority, getTaskStepsByUserId, completeTaskStep
}