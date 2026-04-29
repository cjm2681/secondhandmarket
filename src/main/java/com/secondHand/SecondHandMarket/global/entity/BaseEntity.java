package com.secondHand.SecondHandMarket.global.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;


// @MappedSuperclass: 이 클래스를 상속받는 Entity에 필드를 물려줌
// 자신은 테이블을 갖지 않고, 공통 필드(createdAt, updatedAt)를 하위 Entity에 제공
// 모든 테이블에 반복 선언하지 않아도 됨
@Getter
@MappedSuperclass

// @EntityListeners: JPA Entity 생명주기 이벤트를 감지하는 리스너 등록
// AuditingEntityListener: @CreatedDate, @LastModifiedDate 자동 처리
// 사용하려면 메인 클래스에 @EnableJpaAuditing 필요
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    // @CreatedDate: Entity 최초 저장 시 현재 시간 자동 주입
    // updatable = false: 이후 수정 불가 (insert 시점 값 고정)
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // @LastModifiedDate: Entity 수정될 때마다 현재 시간으로 자동 갱신
    // Dirty Checking으로 UPDATE 발생 시 자동으로 이 값도 함께 업데이트됨
    // 채팅 메시지 순서 보장에 활용: BaseEntity.createdAt을 ORDER BY 기준으로 사용
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
