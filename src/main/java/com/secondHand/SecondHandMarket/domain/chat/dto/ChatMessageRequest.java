package com.secondHand.SecondHandMarket.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageRequest {

    private Long roomId;
    private String message;
    // 발신자는 JWT에서 추출 (STOMP 헤더)
}