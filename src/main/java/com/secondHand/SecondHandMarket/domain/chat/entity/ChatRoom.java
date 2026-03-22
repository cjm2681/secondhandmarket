package com.secondHand.SecondHandMarket.domain.chat.entity;

import com.secondHand.SecondHandMarket.domain.product.entity.Product;
import com.secondHand.SecondHandMarket.domain.user.entity.User;
import com.secondHand.SecondHandMarket.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "chat_room")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class ChatRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;

    // seller_id 제거 → product.seller로 조회
}