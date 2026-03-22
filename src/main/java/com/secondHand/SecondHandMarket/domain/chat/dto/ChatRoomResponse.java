package com.secondHand.SecondHandMarket.domain.chat.dto;

import com.secondHand.SecondHandMarket.domain.chat.entity.ChatRoom;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatRoomResponse {

    private Long roomId;
    private Long productId;
    private String productTitle;
    private String sellerNickname;
    private String buyerNickname;
    private String lastMessage;
    private long unreadCount;
    private LocalDateTime createdAt;

    public static ChatRoomResponse from(ChatRoom room, String lastMessage, long unreadCount) {
        return ChatRoomResponse.builder()
                .roomId(room.getId())
                .productId(room.getProduct().getId())
                .productTitle(room.getProduct().getTitle())
                .sellerNickname(room.getProduct().getSeller().getNickname())
                .buyerNickname(room.getBuyer().getNickname())
                .lastMessage(lastMessage)
                .unreadCount(unreadCount)
                .createdAt(room.getCreatedAt())
                .build();
    }
}