package com.secondHand.SecondHandMarket.domain.chat.service;

import com.secondHand.SecondHandMarket.domain.chat.dto.ChatMessageResponse;
import com.secondHand.SecondHandMarket.domain.chat.dto.ChatRoomResponse;
import com.secondHand.SecondHandMarket.domain.chat.entity.ChatMessage;
import com.secondHand.SecondHandMarket.domain.chat.entity.ChatRoom;
import com.secondHand.SecondHandMarket.domain.chat.repository.ChatMessageRepository;
import com.secondHand.SecondHandMarket.domain.chat.repository.ChatRoomRepository;
import com.secondHand.SecondHandMarket.domain.product.entity.Product;
import com.secondHand.SecondHandMarket.domain.product.repository.ProductRepository;
import com.secondHand.SecondHandMarket.domain.user.Repository.UserRepository;
import com.secondHand.SecondHandMarket.domain.user.entity.User;
import com.secondHand.SecondHandMarket.global.exception.CustomException;
import com.secondHand.SecondHandMarket.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ChatPublisher chatPublisher;

    // 채팅방 조회 또는 생성
    @Transactional
    public ChatRoomResponse getOrCreateRoom(Long productId, Long buyerId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        // 판매자가 본인 상품에 채팅 불가
        if (product.getSeller().getId().equals(buyerId)) {
            throw new CustomException(ErrorCode.CANNOT_CHAT_OWN_PRODUCT);
        }

        ChatRoom room = chatRoomRepository
                .findByProductIdAndBuyerId(productId, buyerId)
                .orElseGet(() -> {
                    User buyer = userRepository.findById(buyerId)
                            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
                    return chatRoomRepository.save(
                            ChatRoom.builder()
                                    .product(product)
                                    .buyer(buyer)
                                    .build()
                    );
                });

        long unreadCount = chatMessageRepository
                .countByRoomIdAndIsReadFalseAndSenderIdNot(room.getId(), buyerId);

        return ChatRoomResponse.from(room, "", unreadCount);
    }

    // 메시지 전송 (STOMP 핸들러에서 호출)
    @Transactional
    public void sendMessage(Long roomId, Long senderId, String message) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        // 채팅방 참여자인지 확인 (구매자 or 판매자)
        boolean isBuyer = room.getBuyer().getId().equals(senderId);
        boolean isSeller = room.getProduct().getSeller().getId().equals(senderId);
        if (!isBuyer && !isSeller) {
            throw new CustomException(ErrorCode.CHAT_ROOM_FORBIDDEN);
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // DB 저장
        ChatMessage chatMessage = ChatMessage.builder()
                .room(room)
                .sender(sender)
                .message(message)
                .build();
        chatMessageRepository.save(chatMessage);

        // Redis Pub/Sub으로 발행
        chatPublisher.publish(ChatMessageResponse.from(chatMessage));
    }

    // 채팅 내역 조회 + 읽음 처리
    @Transactional
    public List<ChatMessageResponse> getMessages(Long roomId, Long userId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        boolean isBuyer = room.getBuyer().getId().equals(userId);
        boolean isSeller = room.getProduct().getSeller().getId().equals(userId);
        if (!isBuyer && !isSeller) {
            throw new CustomException(ErrorCode.CHAT_ROOM_FORBIDDEN);
        }

        // 읽음 처리
        chatMessageRepository.markAllAsRead(roomId, userId);

        return chatMessageRepository.findByRoomId(roomId)
                .stream()
                .map(ChatMessageResponse::from)
                .toList();
    }

    // 내 채팅방 목록
    public List<ChatRoomResponse> getMyRooms(Long userId) {
        return chatRoomRepository.findMyRooms(userId)
                .stream()
                .map(room -> {
                    List<ChatMessage> messages =
                            chatMessageRepository.findByRoomId(room.getId());
                    String lastMessage = messages.isEmpty() ? ""
                            : messages.get(messages.size() - 1).getMessage();
                    long unreadCount = chatMessageRepository
                            .countByRoomIdAndIsReadFalseAndSenderIdNot(room.getId(), userId);
                    return ChatRoomResponse.from(room, lastMessage, unreadCount);
                })
                .toList();
    }


    @Transactional
    public void markAsRead(Long roomId, Long userId) {
        chatMessageRepository.markAllAsRead(roomId, userId);
    }

}