package com.secondHand.SecondHandMarket.domain.chat.repository;

import com.secondHand.SecondHandMarket.domain.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    // 채팅방 중복 생성 방지
    Optional<ChatRoom> findByProductIdAndBuyerId(Long productId, Long buyerId);

    // 내 채팅방 목록 (구매자 or 판매자)
    @Query("""
        SELECT cr FROM ChatRoom cr
        JOIN FETCH cr.product p
        JOIN FETCH p.seller
        JOIN FETCH cr.buyer
        WHERE cr.buyer.id = :userId OR p.seller.id = :userId
        ORDER BY cr.createdAt DESC
        """)
    List<ChatRoom> findMyRooms(@Param("userId") Long userId);
}