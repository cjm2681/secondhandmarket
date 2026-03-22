package com.secondHand.SecondHandMarket.domain.order.dto;

import com.secondHand.SecondHandMarket.domain.order.entity.Orders;
import com.secondHand.SecondHandMarket.domain.order.entity.Payment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class OrderResponse {

    private Long orderId;
    private String tossOrderId;    // ✅ 추가
    private Long productId;
    private String productTitle;
    private int price;
    private String orderStatus;
    private String paymentStatus;
    private String paymentKey;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;

    // 구매자 입장
    private String sellerNickname;

    // 판매자 입장
    private String buyerNickname;

    public static OrderResponse from(Orders order, Payment payment) {
        return OrderResponse.builder()
                .orderId(order.getId())
                .tossOrderId(order.getTossOrderId())   // ✅ 추가
                .productId(order.getProduct().getId())
                .productTitle(order.getProduct().getTitle())
                .price(order.getTotalPrice())
                .orderStatus(order.getStatus().name())
                .paymentStatus(payment.getStatus().name())
                .paymentKey(payment.getPaymentKey())
                .paidAt(payment.getPaidAt())
                .sellerNickname(order.getProduct().getSeller().getNickname())
                .buyerNickname(order.getBuyer().getNickname())
                .createdAt(order.getCreatedAt())
                .build();
    }
}