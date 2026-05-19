package com.secondHand.SecondHandMarket.domain.order.entity;

public enum PaymentStatus {
    READY,
    CONFIRMING,  // 토스 승인 요청 중
    PAID,
    CANCELLED,
    REFUNDED
}