package com.secondHand.SecondHandMarket.global.config;

import com.secondHand.SecondHandMarket.global.exception.CustomException;
import com.secondHand.SecondHandMarket.global.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;


// ChannelInterceptor: STOMP 메시지가 처리되기 전에 가로채는 인터셉터
// HTTP 요청은 JwtFilter에서 인증하지만
// WebSocket은 최초 연결(CONNECT) 이후 HTTP 연결이 끊기므로 별도 인증 필요
@Component
@RequiredArgsConstructor
@Slf4j
public class StompAuthInterceptor implements ChannelInterceptor {

    private JwtProvider jwtProvider;

    // @Autowired setter 주입 사용 이유:
    // WebSocketConfig에서 new StompAuthInterceptor()로 직접 생성하면
    // Spring이 관리하는 Bean이 아니므로 @RequiredArgsConstructor의 생성자 주입 불가
    // → setter로 JwtProvider를 나중에 주입
    @Autowired
    public void setJwtProvider(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) return message;

        // CONNECT 프레임에서만 JWT 검증
        // CONNECT: WebSocket 최초 연결 시 한 번만 발생하는 STOMP 명령
        // SEND, SUBSCRIBE 등 이후 메시지는 이미 인증된 연결이므로 재검증 불필요
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("Authorization");

            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                try {
                    jwtProvider.validateToken(token);
                    Long userId = jwtProvider.getUserId(token);
                    String role = jwtProvider.getRole(token);

                    // accessor.setUser(): 이후 STOMP 메시지에서 Principal로 사용 가능
                    // ChatController의 Principal principal 파라미터로 꺼낼 수 있음
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    userId, null,
                                    List.of(new SimpleGrantedAuthority("ROLE_" + role))
                            );
                    accessor.setUser(auth);
                } catch (CustomException e) {
                    log.warn("WebSocket 인증 실패: {}", e.getMessage());
                    // MessageDeliveryException: STOMP 연결 거부
                    throw new MessageDeliveryException("인증 실패");
                }
            }
        }
        return message;
    }
}