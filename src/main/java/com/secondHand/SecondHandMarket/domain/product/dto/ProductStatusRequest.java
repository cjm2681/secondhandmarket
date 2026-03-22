package com.secondHand.SecondHandMarket.domain.product.dto;

import com.secondHand.SecondHandMarket.domain.product.entity.ProductStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProductStatusRequest {

    @NotNull(message = "변경할 상태를 입력해주세요")
    private ProductStatus status;
}