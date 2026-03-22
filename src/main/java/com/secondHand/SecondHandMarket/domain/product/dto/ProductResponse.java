package com.secondHand.SecondHandMarket.domain.product.dto;

import com.secondHand.SecondHandMarket.domain.product.entity.Product;
import com.secondHand.SecondHandMarket.domain.product.entity.ProductImage;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ProductResponse {

    private Long id;
    private Long sellerId;
    private String sellerNickname;
    private String title;
    private String description;
    private int price;
    private String status;
    private int viewCount;
    private List<String> imageUrls;
    private LocalDateTime createdAt;

    public static ProductResponse from(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .sellerId(product.getSeller().getId())
                .sellerNickname(product.getSeller().getNickname())
                .title(product.getTitle())
                .description(product.getDescription())
                .price(product.getPrice())
                .status(product.getStatus().name())
                .viewCount(product.getViewCount())
                .imageUrls(product.getImages().stream()
                        .map(ProductImage::getImageUrl)
                        .toList())
                .createdAt(product.getCreatedAt())
                .build();
    }
}