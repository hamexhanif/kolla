package team5.prototype.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationServiceImpl(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void sendTaskUpdateNotification(Long taskId, Object payload) {
        // KORREKTUR: Wir erstellen ein dynamisches Ziel (Destination/Topic) pro Task.
        // Das ermöglicht dem Frontend, gezielt Updates für einen bestimmten Task zu abonnieren.
        String destination = String.format("/topic/tasks/%d/updates", taskId);

        logger.info("Sende WebSocket-Update an Destination: {}", destination);
        messagingTemplate.convertAndSend(destination, payload);
    }
}