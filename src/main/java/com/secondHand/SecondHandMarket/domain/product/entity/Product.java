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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)  // 지연 로딩 - 필요할 때만 user 조회
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private int price;

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

    // 이미지 목록 (Product 삭제 시 같이 삭제)
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

    // 상태 변경
    public void updateStatus(ProductStatus status) {
        this.status = status;
    }

    // 조회수 증가
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