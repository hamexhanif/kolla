package team5.prototype.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public NotificationServiceImpl(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void sendTaskUpdateNotification(Long taskId, Object payload) {
        // Wir erstellen ein dynamisches Ziel (Destination/Topic) pro Task.
        // Das ermöglicht dem Frontend, gezielt Updates für einen bestimmten Task zu abonnieren.
        String destination = String.format("/topic/tasks/%d/updates", taskId);

        log.info("Sende WebSocket-Update an Destination: {}", destination);
        messagingTemplate.convertAndSend(destination, payload);
    }
}