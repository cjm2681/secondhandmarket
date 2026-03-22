package com.secondHand.SecondHandMarket.domain.board.dto;

import com.secondHand.SecondHandMarket.domain.board.entity.Board;
import com.secondHand.SecondHandMarket.domain.board.entity.Comment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class BoardResponse {

    private Long id;
    private Long userId;
    private String nickname;
    private String title;
    private String content;
    private int viewCount;
    private List<CommentResponse> comments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static BoardResponse from(Board board, List<Comment> comments) {
        return BoardResponse.builder()
                .id(board.getId())
                .userId(board.getUser().getId())
                .nickname(board.getUser().getNickname())
                .title(board.getTitle())
                .content(board.getContent())
                .viewCount(board.getViewCount())
                .comments(comments.stream()
                        .map(CommentResponse::from)
                        .toList())
                .createdAt(board.getCreatedAt())
                .updatedAt(board.getUpdatedAt())
                .build();
    }
}