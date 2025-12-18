package team5.prototype.taskstep;

import java.util.List;

public interface TaskStepService {

    void assignTaskStepToUser(Long taskStepId, Long userId);

    void completeTaskStep(Long taskStepId, Long userId);

    List<TaskStep> getTaskStepsByUserId(Long userId);

    /**
     * Setzt eine manuelle Priorität und überschreibt die automatische.
     * Erfüllt die TODOs 'overridePriority' und 'setManualPriority'.
     */
    void overridePriority(Long taskStepId, Priority priority);

    /**
     * Erfüllt das TODO 'calculatePriority'.
     * Stößt die Neuberechnung der Priorität für die übergeordnete Task an.
     */
    void calculatePriority(Long taskStepId);

}