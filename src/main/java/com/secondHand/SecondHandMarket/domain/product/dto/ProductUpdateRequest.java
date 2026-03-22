package com.secondHand.SecondHandMarket.domain.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProductUpdateRequest {

    @NotBlank(message = "제목을 입력해주세요")
    private String title;

    private String description;

    @Min(value = 0, message = "가격은 0원 이상이어야 합니다")
    private int price;
}