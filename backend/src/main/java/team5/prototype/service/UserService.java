package team5.prototype.service;

import team5.prototype.entity.TaskStep;
import java.util.List;

public interface UserService {

    /**
     * Liefert alle aktuell zugewiesenen Arbeitsschritte für den Benutzer.
     * @param userId Die ID des Benutzers.
     * @return Eine Liste von TaskStep-Objekten.
     */
    List<TaskStep> getActiveStepsForUser(Long userId);

    /**
     * Ermöglicht eine manuelle Priorisierung durch den Workflowmanager.
     *
     * @param taskStepId Die ID des Arbeitsschritts.
     * @param manualPriority Niedrige Werte bedeuten höhere Priorität. Null entfernt die manuelle Sortierung.
     * @return Der aktualisierte Arbeitsschritt.
     */
    TaskStep overrideManualPriority(Long taskStepId, Integer manualPriority);
}
