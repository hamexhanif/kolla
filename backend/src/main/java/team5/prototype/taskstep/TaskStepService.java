package team5.prototype.taskstep;

public interface TaskStepService {
    // TODO: write method header for assignTaskStepToUser, calculatePriority,
    //       setManualPriority, getTaskStepsByUserId, completeTaskStep

    /**
     * Setzt eine manuelle Priorität für einen TaskStep und überschreibt damit die berechnete Priorität.
     */
    void setManualPriority(Long taskStepId, int manualPriority);
}
