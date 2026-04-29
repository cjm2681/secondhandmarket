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
    // JOIN FETCH 사용 이유:
    //   Product → Seller(User) 관계가 LAZY 로딩
    //   목록 조회 시 각 Product마다 SELECT users 쿼리 추가 발생 (N+1 문제)
    //   JOIN FETCH로 한 번의 쿼리에 Product + Seller 함께 조회
    //
    // :keyword IS NULL → keyword가 null이면 조건 무시 (전체 조회)
    // keyword 있으면 제목 LIKE 검색 적용
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

    // 상세 조회: Seller + Images 한 번에 조회 (N+1 방지)
    // LEFT JOIN FETCH: 이미지 없는 상품도 조회 가능 (INNER JOIN이면 이미지 없는 상품 제외됨)
    // DISTINCT: Product-Image 1:N 조인 시 Product가 이미지 수만큼 중복 조회되는 것 방지
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