package team5.prototype.notification;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public NotificationServiceImpl(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void sendProgressUpdate(Object payload) {
        // Sendet das payload-Objekt an alle Clients, die das Topic "/topic/progress" abonniert haben.
        System.out.println("Sende WebSocket-Update an /topic/progress");
        messagingTemplate.convertAndSend("/topic/progress", payload);
    }
}