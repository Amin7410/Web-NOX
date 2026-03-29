package com.nox.platform.module.engine.infrastructure.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketJwtInterceptor webSocketJwtInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Cấu hình ngọn hướng dẫn cho đường truyền Broadcast (Lắng nghe)
        config.enableSimpleBroker("/topic");
        // Các đường dẫn Frontend bắn lệnh vào Spring Controller
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-studio")
                .setAllowedOriginPatterns("*")
                .withSockJS(); // Fallback support cho client không tương thích thuần WS
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Cài đặt chốt kiểm tra hành lý (Auth interceptor) ở cửa vào
        registration.interceptors(webSocketJwtInterceptor);
    }
}
