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
        // Dies aktiviert einen einfachen, speicherbasierten Message Broker.
        // Das Präfix "/topic" wird für alle Nachrichten verwendet, die an die Clients gesendet werden.
        config.enableSimpleBroker("/topic");
        // Dies definiert das Präfix für Endpunkte, an die Clients Nachrichten senden können (brauchen wir vorerst nicht).
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Dies ist der Endpunkt, mit dem sich das Frontend verbinden wird, um die WebSocket-Verbindung aufzubauen.
        // z.B. ws://localhost:8080/ws
        registry.addEndpoint("/ws").setAllowedOrigins("*"); // Erlaubt Verbindungen von allen Ursprüngen
    }
}