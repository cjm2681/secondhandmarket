package com.secondHand.SecondHandMarket.domain.order.entity;

import com.secondHand.SecondHandMarket.domain.product.entity.Product;
import com.secondHand.SecondHandMarket.domain.user.entity.User;
import com.secondHand.SecondHandMarket.global.entity.BaseEntity;
import com.secondHand.SecondHandMarket.global.exception.CustomException;
import com.secondHand.SecondHandMarket.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Orders extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private int totalPrice;     // 구매 시점 가격 스냅샷

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OrderStatus status = OrderStatus.READY;


    @Column(nullable = false, unique = true)
    private String tossOrderId;  // 토스에 보낼 고유 주문번호

    // 주문 취소
    public void cancel() {
        if (this.status != OrderStatus.PAID) {
            throw new CustomException(ErrorCode.ORDER_CANNOT_CANCEL);
        }
        this.status = OrderStatus.CANCELLED;
    }

    // 결제 완료
    public void paid() {
        this.status = OrderStatus.PAID;
    }
}