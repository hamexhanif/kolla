package team5.prototype.notification;

public interface NotificationService {

    /**
     * Sendet eine Benachrichtigung über eine Aktualisierung eines spezifischen Tasks.
     * @param taskId Die ID des Tasks, der aktualisiert wurde.
     * @param payload Das Objekt, das die Update-Informationen enthält (z.B. eine Nachricht oder ein DTO).
     */
    void sendTaskUpdateNotification(Long taskId, Object payload);
}