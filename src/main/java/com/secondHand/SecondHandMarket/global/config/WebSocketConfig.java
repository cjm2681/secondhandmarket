package com.secondHand.SecondHandMarket.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;


// @EnableWebSocketMessageBroker: Spring에서 STOMP 프로토콜을 사용한 WebSocket 메시지 브로커 활성화
// WebSocket 위에 STOMP를 얹는 이유:
//   순수 WebSocket은 단순 바이트 스트림 → 채널 구분, 메시지 라우팅 불가
//   STOMP는 pub/sub 모델 제공 → /sub/chat/{roomId} 같은 목적지 기반 라우팅 가능
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // In-Memory 브로커 활성화
        // 클라이언트가 /sub/chat/{roomId} 구독 → 해당 목적지로 오는 메시지 수신
        registry.enableSimpleBroker("/sub");

        // 클라이언트 → 서버 메시지 발행 prefix
        // 클라이언트가 /pub/chat/{roomId} 로 전송 → @MessageMapping("/chat/{roomId}") 로 라우팅
        registry.setApplicationDestinationPrefixes("/pub");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-chat")       // WebSocket 최초 연결 엔드포인트
                .setAllowedOriginPatterns("*")
                .withSockJS();                // WebSocket 미지원 브라우저를 위한 폴백 (HTTP Long Polling 등)
    }

    // STOMP CONNECT 시점에 JWT 검증 인터셉터 등록
    // HTTP 요청은 JwtFilter에서 처리하지만
    // WebSocket은 최초 연결(CONNECT) 이후 HTTP와 분리되므로 별도 인터셉터 필요
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompAuthInterceptor());
    }

    @Bean
    public StompAuthInterceptor stompAuthInterceptor() {
        return new StompAuthInterceptor();
    }
}