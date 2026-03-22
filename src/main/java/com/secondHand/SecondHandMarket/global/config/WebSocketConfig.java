package com.secondHand.SecondHandMarket.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 구독 prefix: 클라이언트가 /sub/chat/{roomId} 구독
        registry.enableSimpleBroker("/sub");
        // 발행 prefix: 클라이언트가 /pub/chat 으로 메시지 전송
        registry.setApplicationDestinationPrefixes("/pub");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-chat")      // WebSocket 연결 엔드포인트
                .setAllowedOriginPatterns("*")
                .withSockJS();                // SockJS fallback 지원
    }

    // STOMP 헤더에서 JWT 검증
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompAuthInterceptor());
    }

    @Bean
    public StompAuthInterceptor stompAuthInterceptor() {
        return new StompAuthInterceptor();
    }
}