package team5.prototype.task;

public interface TaskService {

    /**
     * Erstellt eine neue Task basierend auf einer WorkflowDefinition.
     *
     * @param request vollständige Beschreibung für die Instanziierung.
     * @return Die neu erstellte Task.
     */
    Task createTaskFromDefinition(TaskCreationRequest request);

    /**
     * Schließt einen Arbeitsschritt ab und bewegt den Workflow vorwärts.
     *
     * @param taskId Die ID der übergeordneten Aufgabe.
     * @param stepId Die ID des abzuschließenden Arbeitsschritts.
     * @param userId Die ID des Benutzers, der die Aktion ausführt.
     */
    void completeStep(Long taskId, Long stepId, Long userId);

    /**
     * Liefert eine kompakte Fortschrittsübersicht für den Workflowmanager.
     */
    TaskProgress getTaskProgress(Long taskId);
}
