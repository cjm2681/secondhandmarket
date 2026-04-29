package com.secondHand.SecondHandMarket.domain.product.entity;

import com.secondHand.SecondHandMarket.domain.user.entity.User;
import com.secondHand.SecondHandMarket.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // JPA는 기본 생성자 필수
                                                    // PROTECTED로 막아서 외부에서 new Product() 직접 호출 방지
                                                    // → Builder 패턴 강제
@Builder
@AllArgsConstructor
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 동시 결제 Race Condition 방지용 낙관적 락(Optimistic Lock)
    // 비관적 락(PESSIMISTIC_WRITE)은 SELECT ... FOR UPDATE로 DB row를 잠금
    //   → 외부 API(토스) 호출 시간(1~3초) 동안 락 점유 → 성능 저하
    // 낙관적 락은 커밋 시점에만 version 조건 체크 → 락 점유 시간 0

    // JPA가 커밋 시 자동 생성하는 쿼리:
    //   UPDATE product SET status='RESERVED', version=N+1
    //   WHERE id=? AND version=N  ← 읽었던 version과 일치해야 성공
    //   영향 row = 0 → 다른 사람이 먼저 변경 → OptimisticLockException
    @Version
    @Builder.Default
    private Long version = 0L;  // 초기값 0L 필수
                                // @Builder 사용 시 명시 안 하면 null → 낙관적 락 NPE 발생


    // FetchType.LAZY: 상품 조회 시 판매자 정보를 즉시 로딩하지 않음
    // 상품 목록 조회에서 판매자 정보가 필요 없는 경우 불필요한 JOIN 쿼리 방지
    // 실제 판매자 정보 접근 시점(seller.getNickname() 등)에 쿼리 실행
    @ManyToOne(fetch = FetchType.LAZY)  // 지연 로딩 - 필요할 때만 user 조회
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private int price;


    // @Enumerated(EnumType.STRING): DB에 숫자(0,1,2)가 아닌 "SALE","RESERVED","SOLD" 문자열로 저장
    // EnumType.ORDINAL(기본값) 사용 시 Enum 순서 변경만으로 기존 데이터 오염 위험
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ProductStatus status = ProductStatus.SALE;

    @Column(nullable = false)
    @Builder.Default
    private int viewCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private boolean isDeleted = false;

    // cascade = CascadeType.ALL: Product 저장/삭제 시 연관 이미지도 함께 처리
    // orphanRemoval = true: Product에서 이미지 제거 시(images.clear()) DB에서도 자동 삭제
    //   → product.getImages().clear() 호출만으로 이미지 전체 삭제 가능
    // @OrderBy: DB 레벨에서 정렬 → 이미지 순서 항상 보장
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @OrderBy("sortOrder ASC")
    private List<ProductImage> images = new ArrayList<>();

    // 수정
    public void update(String title, String description, int price) {
        this.title = title;
        this.description = description;
        this.price = price;
    }

    // updateStatus()가 낙관적 락의 핵심 진입점
    // 이 메서드 호출 후 트랜잭션 커밋 시 JPA가 version 조건 포함 UPDATE 자동 실행
    public void updateStatus(ProductStatus status) {
        this.status = status;
    }

    // Dirty Checking 활용 - 별도 save() 호출 없이 트랜잭션 커밋 시 자동 UPDATE
    public void increaseViewCount() {
        this.viewCount++;
    }

    // 소프트 삭제
    public void delete() {
        this.isDeleted = true;
    }

    // 이미지 추가
    public void addImage(ProductImage image) {
        this.images.add(image);
        image.setProduct(this);
    }
}