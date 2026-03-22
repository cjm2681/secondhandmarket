package com.secondHand.SecondHandMarket.domain.order.repository;

import com.secondHand.SecondHandMarket.domain.order.entity.OrderStatus;
import com.secondHand.SecondHandMarket.domain.order.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Orders, Long> {

    // 내 구매 내역 (최신순)
    @Query("""
        SELECT o FROM Orders o
        JOIN FETCH o.product p
        JOIN FETCH p.seller
        WHERE o.buyer.id = :buyerId
        ORDER BY o.createdAt DESC
        """)
    List<Orders> findByBuyerIdWithProduct(@Param("buyerId") Long buyerId);

    // 판매자 입장 판매 내역
    @Query("""
        SELECT o FROM Orders o
        JOIN FETCH o.product p
        JOIN FETCH o.buyer
        WHERE p.seller.id = :sellerId
        ORDER BY o.createdAt DESC
        """)
    List<Orders> findBySellerId(@Param("sellerId") Long sellerId);

    // 특정 상품 주문 여부 확인 (중복 구매 방지)
    boolean existsByBuyerIdAndProductId(Long buyerId, Long productId);


    boolean existsByBuyerIdAndProductIdAndStatus(
            Long buyerId, Long productId, OrderStatus status);

    // 기존 deleteBy 대신 findBy로 변경
    Optional<Orders> findByBuyerIdAndProductIdAndStatus(
            Long buyerId, Long productId, OrderStatus status);

    Optional<Orders> findByTossOrderId(String tossOrderId);

}