package com.secondHand.SecondHandMarket.domain.order.entity;

import com.secondHand.SecondHandMarket.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Orders order;

    @Column(nullable = false)
    private String paymentKey;  // Mock: "MOCK_{UUID}", 실제 PG 연동 시 PG사 키

    @Column(nullable = false)
    private int amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.READY;

    private LocalDateTime paidAt;

    // 결제 완료 처리
    public void paid() {
        this.status = PaymentStatus.PAID;
        this.paidAt = LocalDateTime.now();
    }

    // 환불 처리
    public void refund() {
        this.status = PaymentStatus.REFUNDED;
    }


    public void confirm(String paymentKey) {
        this.paymentKey = paymentKey;
        this.status = PaymentStatus.PAID;
        this.paidAt = LocalDateTime.now();
    }

}