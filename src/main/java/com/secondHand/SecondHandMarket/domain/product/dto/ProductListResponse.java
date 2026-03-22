package com.secondHand.SecondHandMarket.domain.product.dto;

import com.secondHand.SecondHandMarket.domain.product.entity.Product;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// 목록용 - description, 전체 이미지 제외하고 대표 이미지 1장만
@Getter
@Builder
public class ProductListResponse {

    private Long id;
    private String sellerNickname;
    private String title;
    private int price;
    private String status;
    private int viewCount;
    private String thumbnailUrl;   // 대표 이미지 1장
    private LocalDateTime createdAt;

    public static ProductListResponse from(Product product) {
        String thumbnail = product.getImages().isEmpty()
                ? null
                : product.getImages().get(0).getImageUrl();

        return ProductListResponse.builder()
                .id(product.getId())
                .sellerNickname(product.getSeller().getNickname())
                .title(product.getTitle())
                .price(product.getPrice())
                .status(product.getStatus().name())
                .viewCount(product.getViewCount())
                .thumbnailUrl(thumbnail)
                .createdAt(product.getCreatedAt())
                .build();
    }
}