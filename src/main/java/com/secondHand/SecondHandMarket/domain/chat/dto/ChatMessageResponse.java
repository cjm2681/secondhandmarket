package com.secondHand.SecondHandMarket.domain.chat.dto;

import com.secondHand.SecondHandMarket.domain.chat.entity.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse implements Serializable {  // Redis 직렬화용

    private Long id;
    private Long roomId;
    private Long senderId;
    private String senderNickname;
    private String message;
    private boolean read;
    private LocalDateTime createdAt;

    public static ChatMessageResponse from(ChatMessage msg) {
        return ChatMessageResponse.builder()
                .id(msg.getId())
                .roomId(msg.getRoom().getId())
                .senderId(msg.getSender().getId())
                .senderNickname(msg.getSender().getNickname())
                .message(msg.getMessage())
                .read(msg.isRead())
                .createdAt(msg.getCreatedAt())
                .build();
    }
}