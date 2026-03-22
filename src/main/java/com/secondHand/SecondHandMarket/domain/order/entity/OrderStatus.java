package com.secondHand.SecondHandMarket.domain.order.entity;

public enum OrderStatus {
    READY,      // 주문 생성
    PAID,       // 결제 완료
    CANCELLED   // 취소
}