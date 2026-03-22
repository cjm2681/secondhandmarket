package com.secondHand.SecondHandMarket.domain.product.repository;

import com.secondHand.SecondHandMarket.domain.product.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    void deleteByProductId(Long productId);
}