package com.secondHand.SecondHandMarket.domain.product.repository;

import com.secondHand.SecondHandMarket.domain.product.entity.ViewHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ViewHistoryRepository extends JpaRepository<ViewHistory, Long> {

    boolean existsByTargetTypeAndTargetIdAndViewer(
            String targetType, Long targetId, String viewer);
}