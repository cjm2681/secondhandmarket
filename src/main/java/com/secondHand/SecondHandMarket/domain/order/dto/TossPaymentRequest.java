package com.secondHand.SecondHandMarket.domain.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TossPaymentRequest {

    @NotBlank
    private String paymentKey;  // 토스에서 발급한 결제 키

    @NotBlank
    private String orderId;     // 우리가 만든 주문 ID (String 형태)

    @NotNull
    private Long amount;        // 결제 금액 (검증용)
}