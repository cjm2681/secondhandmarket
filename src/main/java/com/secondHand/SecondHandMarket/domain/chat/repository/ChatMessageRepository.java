package com.secondHand.SecondHandMarket.domain.chat.repository;

import com.secondHand.SecondHandMarket.domain.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // 채팅 메시지 목록 (오래된 순)
    @Query("""
        SELECT m FROM ChatMessage m
        JOIN FETCH m.sender
        WHERE m.room.id = :roomId
        ORDER BY m.createdAt ASC
        """)
    List<ChatMessage> findByRoomId(@Param("roomId") Long roomId);

    // 읽음 처리 (내가 보낸 메시지 제외)
    @Modifying
    @Query("""
        UPDATE ChatMessage m SET m.isRead = true
        WHERE m.room.id = :roomId AND m.sender.id != :userId
        """)
    void markAllAsRead(@Param("roomId") Long roomId, @Param("userId") Long userId);

    // 안 읽은 메시지 수
    long countByRoomIdAndIsReadFalseAndSenderIdNot(Long roomId, Long userId);
}