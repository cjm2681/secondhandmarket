package com.secondHand.SecondHandMarket.domain.product.repository;

import com.secondHand.SecondHandMarket.domain.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // 목록 조회 - 삭제 안 된 것만, 제목 검색 + 페이징
    @Query("""
        SELECT p FROM Product p
        JOIN FETCH p.seller
        WHERE p.isDeleted = false
        AND (:keyword IS NULL OR p.title LIKE %:keyword%)
        ORDER BY p.createdAt DESC
        """)
    Page<Product> findAllByKeyword(
            @Param("keyword") String keyword,
            Pageable pageable
    );

    // 상세 조회 - 이미지까지 한 번에 (N+1 방지)
    @Query("""
        SELECT DISTINCT p FROM Product p
        JOIN FETCH p.seller
        LEFT JOIN FETCH p.images
        WHERE p.id = :id AND p.isDeleted = false
        """)
    Optional<Product> findByIdWithImages(@Param("id") Long id);

    // 내가 올린 판매글
    Page<Product> findBySellerIdAndIsDeletedFalse(Long sellerId, Pageable pageable);
}