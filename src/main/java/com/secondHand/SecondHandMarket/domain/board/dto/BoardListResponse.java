package com.secondHand.SecondHandMarket.domain.board.dto;

import com.secondHand.SecondHandMarket.domain.board.entity.Board;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BoardListResponse {

    private Long id;
    private String nickname;
    private String title;
    private int viewCount;
    private int commentCount;
    private LocalDateTime createdAt;

    public static BoardListResponse from(Board board) {
        return BoardListResponse.builder()
                .id(board.getId())
                .nickname(board.getUser().getNickname())
                .title(board.getTitle())
                .viewCount(board.getViewCount())
                .commentCount((int) board.getComments().stream()
                        .filter(c -> !c.isDeleted()).count())
                .createdAt(board.getCreatedAt())
                .build();
    }
}