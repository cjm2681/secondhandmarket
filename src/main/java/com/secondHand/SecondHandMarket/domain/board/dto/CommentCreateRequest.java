package com.secondHand.SecondHandMarket.domain.board.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CommentCreateRequest {

    @NotBlank(message = "내용을 입력해주세요")
    private String content;

    // 대댓글일 경우 부모 댓글 ID, 일반 댓글이면 null
    private Long parentId;
}