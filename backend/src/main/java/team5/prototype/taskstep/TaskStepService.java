package team5.prototype.taskstep;

import team5.prototype.dto.ActorDashboardItemDto;
import java.util.List;

public interface TaskStepService {

    // KORREKTUR: Wir fügen die Methode hinzu, die der Controller braucht
    List<ActorDashboardItemDto> getActorDashboardItems(Long userId, TaskStepStatus status, Priority priority, String query);

    // Diese Methode ist für den einfachen Aufruf aus dem UserController
    List<ActorDashboardItemDto> getActorDashboardItems(Long userId);

    TaskStepDto setManualPriorityAndConvertToDto(Long taskStepId, int manualPriority);
    void completeTaskStep(Long taskId, Long taskStepId, Long userId);
    List<TaskStep> getAllTaskStepsByUserId(Long userId);
}
