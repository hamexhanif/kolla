package team5.prototype.task;

import java.util.List;
import java.util.Optional;

public interface TaskService {

    /**
     * Erstellt eine neue Task aus einer Vorlage, übergeben als DTO.
     * @param request Ein DTO, das alle notwendigen Informationen für die Erstellung enthält.
     * @return Die neu erstellte und in der Datenbank gespeicherte Task-Entität.
     */
    Task createTaskFromDefinition(TaskDto request);

    /**
     * Schließt einen Arbeitsschritt ab und bewegt den Workflow vorwärts.
     *
     * @param taskId Die ID der übergeordneten Aufgabe.
     * @param stepId Die ID des abzuschließenden Arbeitsschritts.
     * @param userId Die ID des Benutzers, der die Aktion ausführt.
     */
    void completeStep(Long taskId, Long stepId, Long userId);

    /**
     * Ruft den aktuellen Fortschritt einer Task ab.
     * @param taskId Die ID der abzufragenden Task.
     * @return Ein DTO, das die Fortschrittsinformationen zusammenfasst.
     */
    TaskProgress getTaskProgress(Long taskId); // Gibt jetzt das spezifische TaskProgress-Objekt zurück

    /**
     * Ruft alle Tasks ab.
     * @return Eine Liste aller Task-Entitäten.
     */
    List<Task> getAllTasks();

    /**
     * Ruft eine einzelne Task anhand ihrer ID ab.
     * @param taskId Die ID der zu suchenden Task.
     * @return Ein Optional, das die Task-Entität enthält, falls gefunden.
     */
    Optional<Task> getTaskById(Long taskId);

}