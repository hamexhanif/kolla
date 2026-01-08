package team5.prototype.notification;

import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class NotificationServiceImplTest {

    @Test
    void sendTaskUpdateNotificationUsesTaskTopic() {
        SimpMessagingTemplate template = mock(SimpMessagingTemplate.class);
        NotificationServiceImpl service = new NotificationServiceImpl(template);

        service.sendTaskUpdateNotification(42L, "payload");

        verify(template).convertAndSend("/topic/tasks/42/updates", "payload");
    }
}
