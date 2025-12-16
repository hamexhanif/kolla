package team5.prototype.task;

import java.util.List;
import java.util.Optional;

public interface TaskService {

    /**
     * Erstellt eine neue Task basierend auf einer WorkflowDefinition.
     * @param definitionId Die ID der Vorlage.
     * @param title Der Titel für die neue, konkrete Aufgabe.
     * @return Die neu erstellte Task.
     */
    Task createTaskFromDefinition(Long definitionId, String title);
//    /**
//     * Schließt einen Arbeitsschritt ab und bewegt den Workflow vorwärts.
//     * @param taskId Die ID der übergeordneten Aufgabe.
//     * @param stepId Die ID des abzuschließenden Arbeitsschritts.
//     * @param userId Die ID des Benutzers, der die Aktion ausführt.
//     */
    void completeStep(Long taskId, Long stepId, String userId);
    List<Task> getAllTasks();
    Optional<Task> getTaskById(Long taskId);
    void deleteTask(Long taskId);
//    the method/function above belongs to the TaskStepService

}