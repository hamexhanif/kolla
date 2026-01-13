package team5.prototype.taskstep;

import team5.prototype.dto.ActorDashboardItemDto;
import java.util.List;

public interface TaskStepService {

    List<ActorDashboardItemDto> getActorDashboardItems(Long userId, TaskStepStatus status, Priority priority, String query);
    List<ActorDashboardItemDto> getActorDashboardItems(Long userId);
    TaskStepDto setManualPriorityAndConvertToDto(Long taskStepId, int manualPriority);
    void completeTaskStep(Long taskId, Long taskStepId, Long userId);
    List<TaskStep> getAllTaskStepsByUserId(Long userId);
}
