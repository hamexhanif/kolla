package team5.prototype.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Aktiviert einen einfachen Message Broker, der Nachrichten an Clients
        // an Ziele weiterleitet, die mit "/topic" beginnen.
        config.enableSimpleBroker("/topic");

        // Definiert das Präfix "/app" für Nachrichten, die von Clients
        // an @MessageMapping-annotierte Methoden im Backend gesendet werden.
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Registriert den "/ws" Endpunkt, den Clients für die WebSocket-Verbindung nutzen.
        // setAllowedOrigins("*") erlaubt Verbindungen von jeder Domain (wichtig für die Entwicklung).
        registry.addEndpoint("/ws").setAllowedOrigins("*");
    }
}