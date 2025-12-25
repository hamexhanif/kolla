package team5.prototype.taskstep;

import team5.prototype.dto.ActorDashboardItemDto;

import java.util.List;

public interface TaskStepService {

    /**
     * Ordnet einen TaskStep einem Benutzer zu und setzt den Status auf ASSIGNED.
     */
    TaskStep assignTaskStepToUser(Long taskStepId, Long userId);

    /**
     * Setzt eine manuelle Priorität und überschreibt die automatische.
     * Erfüllt die TODOs 'overridePriority' und 'setManualPriority'.
     */
    TaskStep setManualPriority(Long taskStepId, int manualPriority);

    /**
     * Liefert alle nicht abgeschlossenen TaskSteps eines Nutzers.
     */
    List<TaskStep> getActiveTaskStepsByUser(Long userId);

    /**
     * Liefert Dashboard-Eintraege fuer einen Nutzer mit optionalen Filtern.
     */
    List<ActorDashboardItemDto> getActorDashboardItems(Long userId,
                                                       TaskStepStatus status,
                                                       Priority priority,
                                                       String query);

    /**
     * Schließt einen TaskStep ab (inkl. Fortschritts-Update auf der zugehörigen Task).
     */
    void completeTaskStep(Long taskId, Long taskStepId, Long userId);
}
