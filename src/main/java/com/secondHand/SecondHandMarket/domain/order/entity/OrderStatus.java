package com.secondHand.SecondHandMarket.domain.order.entity;

public enum OrderStatus {
    READY,      // 주문 생성
    CONFIRMING,  // 토스 승인 요청 중 (불일치 감지용)
    PAID,       // 결제 완료
    CANCELLED   // 취소
}