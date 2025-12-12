package team5.prototype.service;

import team5.prototype.entity.Task;
import java.util.List;

public interface UserService {

    /**
     * Ruft alle zugewiesenen und offenen Aufgaben für einen bestimmten Benutzer ab.
     * @param userId Die ID des Benutzers.
     * @return Eine Liste von Task-Objekten.
     */
    List<Task> getTasksForUser(String userId);
    // Hinweis: Wir verwenden String für userId, falls Sie UUIDs verwenden. Passen Sie es ggf. an Long an.
}