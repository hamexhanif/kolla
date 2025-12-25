package team5.prototype.task;

import team5.prototype.dto.TaskDetailsDto;

import java.util.List;
import java.util.Optional;

public interface TaskService {

    /**
     * Erstellt eine neue Task aus einer Vorlage, uebergeben als DTO.
     * @param request Ein DTO, das alle notwendigen Informationen fuer die Erstellung enthaelt.
     * @return Die neu erstellte und in der Datenbank gespeicherte Task-Entitaet.
     */
    Task createTaskFromDefinition(TaskDto request);

    /**
     * Schliesst einen Arbeitsschritt ab und bewegt den Workflow vorwaerts.
     *
     * @param taskId Die ID der uebergeordneten Aufgabe.
     * @param stepId Die ID des abzuschliessenden Arbeitsschritts.
     * @param userId Die ID des Benutzers, der die Aktion ausfuehrt.
     */
    void completeStep(Long taskId, Long stepId, Long userId);

    /**
     * Ruft den aktuellen Fortschritt einer Task ab.
     * @param taskId Die ID der abzufragenden Task.
     * @return Ein DTO, das die Fortschrittsinformationen zusammenfasst.
     */
    TaskProgress getTaskProgress(Long taskId);

    /**
     * Ruft alle Tasks ab.
     * @return Eine Liste aller Task-Entitaeten.
     */
    List<Task> getAllTasks();

    /**
     * Ruft eine einzelne Task anhand ihrer ID ab.
     * @param taskId Die ID der zu suchenden Task.
     * @return Ein Optional, das die Task-Entitaet enthaelt, falls gefunden.
     */
    Optional<Task> getTaskById(Long taskId);

    /**
     * Liefert die Detailansicht einer Task mit Schritten fuer das Akteur-Dashboard.
     */
    TaskDetailsDto getTaskDetails(Long taskId);
}
