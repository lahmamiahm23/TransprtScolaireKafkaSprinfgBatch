package com.example.transport.config;

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
        // Active un broker simple en mémoire pour envoyer des messages du serveur vers le client
        config.enableSimpleBroker("/topic");
        // Préfixe pour les messages envoyés du client vers le serveur (si besoin)
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Point d'entrée pour la connexion du Frontend (React)
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:3000") // L'URL de votre application React
                .withSockJS();
    }
}
