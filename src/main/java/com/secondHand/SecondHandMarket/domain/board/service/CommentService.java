package com.secondHand.SecondHandMarket.domain.board.service;

import com.secondHand.SecondHandMarket.domain.board.dto.CommentCreateRequest;
import com.secondHand.SecondHandMarket.domain.board.dto.CommentResponse;
import com.secondHand.SecondHandMarket.domain.board.entity.Board;
import com.secondHand.SecondHandMarket.domain.board.entity.Comment;
import com.secondHand.SecondHandMarket.domain.board.repository.BoardRepository;
import com.secondHand.SecondHandMarket.domain.board.repository.CommentRepository;
import com.secondHand.SecondHandMarket.domain.user.Repository.UserRepository;
import com.secondHand.SecondHandMarket.domain.user.entity.User;
import com.secondHand.SecondHandMarket.global.exception.CustomException;
import com.secondHand.SecondHandMarket.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    // 댓글 작성 (일반 댓글 + 대댓글 통합)
    @Transactional
    public CommentResponse create(Long boardId, Long userId, CommentCreateRequest request) {
        Board board = boardRepository.findById(boardId)
                .filter(b -> !b.isDeleted())
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Comment parent = null;

        // parentId가 있으면 대댓글 처리
        if (request.getParentId() != null) {
            parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new CustomException(ErrorCode.PARENT_COMMENT_NOT_FOUND));

            // 보안 검증: 부모 댓글이 현재 게시글 소속인지 확인
            // URL의 boardId와 부모 댓글의 board.id가 다르면 비정상 요청
            if (!parent.getBoard().getId().equals(boardId)) {
                throw new CustomException(ErrorCode.PARENT_COMMENT_NOT_FOUND);
            }


            // 2depth 제한: 대댓글에 또 대댓글 금지
            // parent.getParent() != null → 이미 대댓글인 댓글에 또 달려는 시도
            if (parent.getParent() != null) {
                throw new CustomException(ErrorCode.REPLY_DEPTH_EXCEEDED);
            }
        }

        Comment comment = Comment.builder()
                .board(board)
                .user(user)
                .content(request.getContent())
                .parent(parent)
                .build();

        return CommentResponse.from(commentRepository.save(comment));
    }

    // 댓글 수정
    @Transactional
    public CommentResponse update(Long commentId, Long userId, String content) {
        Comment comment = getCommentOrThrow(commentId);
        checkOwnership(comment, userId);

        comment.update(content);
        return CommentResponse.from(comment);
    }

    // 댓글 삭제 (소프트 삭제)
    // 대댓글이 있으면 "삭제된 댓글입니다" 표시, 없으면 완전 삭제
    @Transactional
    public void delete(Long commentId, Long userId, String role) {
        Comment comment = getCommentOrThrow(commentId);

        if (!comment.getUser().getId().equals(userId) && !role.equals("ADMIN")) {
            throw new CustomException(ErrorCode.COMMENT_FORBIDDEN);
        }

        if (!comment.getChildren().isEmpty()) {
            // 대댓글 있으면 소프트 삭제
            comment.delete();
        } else {
            // 대댓글 없으면 완전 삭제
            commentRepository.delete(comment);
        }
    }

    private Comment getCommentOrThrow(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));
    }

    private void checkOwnership(Comment comment, Long userId) {
        if (!comment.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.COMMENT_FORBIDDEN);
        }
    }
}