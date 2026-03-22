package com.secondHand.SecondHandMarket.domain.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrderCreateRequest {

    @NotNull(message = "상품 ID를 입력해주세요")
    private Long productId;
}