package com.secondHand.SecondHandMarket.domain.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProductCreateRequest {

    @NotBlank(message = "제목을 입력해주세요")
    @Size(max = 200, message = "제목은 200자 이하여야 합니다")
    private String title;

    private String description;

    @Min(value = 0, message = "가격은 0원 이상이어야 합니다")
    private int price;
}