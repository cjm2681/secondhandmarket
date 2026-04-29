package com.secondHand.SecondHandMarket.domain.admin.service;

import com.secondHand.SecondHandMarket.domain.admin.dto.AdminUserResponse;
import com.secondHand.SecondHandMarket.domain.board.entity.Board;
import com.secondHand.SecondHandMarket.domain.board.entity.Comment;
import com.secondHand.SecondHandMarket.domain.board.repository.BoardRepository;
import com.secondHand.SecondHandMarket.domain.board.repository.CommentRepository;
import com.secondHand.SecondHandMarket.domain.product.entity.Product;
import com.secondHand.SecondHandMarket.domain.product.repository.ProductRepository;
import com.secondHand.SecondHandMarket.domain.user.Repository.UserRepository;
import com.secondHand.SecondHandMarket.domain.user.entity.Role;
import com.secondHand.SecondHandMarket.domain.user.entity.User;
import com.secondHand.SecondHandMarket.domain.user.entity.UserStatus;
import com.secondHand.SecondHandMarket.global.exception.CustomException;
import com.secondHand.SecondHandMarket.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본적으로 읽기 전용 트랜잭션
                                // SELECT 쿼리에서 불필요한 Dirty Checking 스킵 → 성능 최적화
                                // 쓰기가 필요한 메서드에만 @Transactional 별도 선언
public class AdminService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;

    // 전체 회원 목록 조회
    public Page<AdminUserResponse> getUsers(String keyword, int page) {
        Pageable pageable = PageRequest.of(page, 20, Sort.by("createdAt").descending());
        return userRepository.findAllByKeyword(keyword, pageable)
                .map(AdminUserResponse::from);
    }

    // 특정 회원 정보 조회 (닉네임 클릭 시)
    public AdminUserResponse getUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return AdminUserResponse.from(user);
    }

    // 회원 정지 / 정지 해제 (상태 보고 자동 토글)
    @Transactional
    public AdminUserResponse toggleBan(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 어드민 계정은 정지 불가
        if (user.getRole() == Role.ADMIN) {
            throw new CustomException(ErrorCode.CANNOT_BAN_ADMIN);
        }

        // 현재 상태 보고 토글
        // User 엔티티의 ban(), activate() 메서드가 상태 변경 → Dirty Checking으로 자동 UPDATE
        if (user.getStatus() == UserStatus.ACTIVE) {
            user.ban();
        } else {
            user.activate();
        }

        return AdminUserResponse.from(user);
    }

    // 판매글 삭제 (소프트 삭제)
    // DB에서 실제 삭제하지 않고 is_deleted = true 로 표시만 함
    // 이유: 주문/결제 이력이 연결되어 있어 실제 삭제 시 데이터 정합성 문제 발생 가능
    @Transactional
    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
        product.delete();   // is_deleted = true → Dirty Checking으로 자동 UPDATE
    }

    // 게시글 삭제 (소프트 삭제)
    @Transactional
    public void deleteBoard(Long boardId) {
        Board board = boardRepository.findById(boardId)
                .filter(b -> !b.isDeleted())
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));
        board.delete();
    }

    // 댓글/대댓글 삭제
    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        if (!comment.getChildren().isEmpty()) {
            // 대댓글이 있는 부모 댓글 → 소프트 삭제
            // 완전 삭제 시 대댓글의 맥락이 사라지는 UX 문제 방지
            // 화면에는 "삭제된 댓글입니다."로 표시
            comment.delete();
        } else {
            // 대댓글 없는 댓글 → 완전 삭제
            // 불필요한 데이터 누적 방지
            commentRepository.delete(comment);
        }
    }
}