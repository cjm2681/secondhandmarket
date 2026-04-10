package com.secondHand.SecondHandMarket.domain.product.entity;

import com.secondHand.SecondHandMarket.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "view_history",
    uniqueConstraints = @UniqueConstraint(columnNames = {"target_type", "target_id", "viewer"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class ViewHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String targetType;      // "PRODUCT" or "BOARD"

    @Column(nullable = false)
    private Long targetId;

    @Column(nullable = false)
    private String viewer;  //userId or IP
}
