package com.secondHand.SecondHandMarket.domain.board.dto;

import com.secondHand.SecondHandMarket.domain.board.entity.Comment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class CommentResponse {

    private Long id;
    private Long userId;
    private String nickname;
    private String content;
    private boolean isDeleted;
    private List<CommentResponse> children;  // 대댓글 목록
    private LocalDateTime createdAt;

    public static CommentResponse from(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .userId(comment.getUser().getId())
                .nickname(comment.getUser().getNickname())
                // 삭제된 댓글은 내용 숨김 처리 (트위터/유튜브 방식)
                .content(comment.isDeleted() ? "삭제된 댓글입니다." : comment.getContent())
                .isDeleted(comment.isDeleted())
                .children(comment.getChildren().stream()
                        .filter(child -> !child.isDeleted()
                                || !child.getChildren().isEmpty())
                        .map(CommentResponse::from)
                        .toList())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}