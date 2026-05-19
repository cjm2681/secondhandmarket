package com.secondHand.SecondHandMarket.domain.order.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TossWebhookRequest {

    private String eventType;  // "PAYMENT_STATUS_CHANGED"
    private WebhookData data;

    @Getter
    @NoArgsConstructor
    public static class WebhookData {
        private String paymentKey;
        private String orderId;    // 우리가 만든 tossOrderId
        private String status;     // "DONE", "ABORTED" 등
    }
}