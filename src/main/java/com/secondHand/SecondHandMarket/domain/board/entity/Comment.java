package com.secondHand.SecondHandMarket.domain.board.entity;

import com.secondHand.SecondHandMarket.domain.user.entity.User;
import com.secondHand.SecondHandMarket.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "comment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    @Builder.Default
    private boolean isDeleted = false;

    // 대댓글 구현 - 자기 자신 참조
    // parent == null → 일반 댓글
    // parent != null → 대댓글 (부모 댓글의 children 리스트에 포함됨)
    // FetchType.LAZY: 댓글 조회 시 부모 댓글을 즉시 로딩하지 않음 → N+1 문제 방지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    // 자식 댓글(대댓글) 목록
    // cascade = CascadeType.ALL: 부모 댓글 삭제 시 대댓글도 함께 삭제
    // orphanRemoval = true: 부모로부터 분리된 자식(고아 객체) 자동 삭제
    // 단, 소프트 삭제 시에는 cascade 실행 안 됨 (isDeleted=true만 변경이므로)
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Comment> children = new ArrayList<>();

    public void update(String content) {
        this.content = content;
    }

    public void delete() {
        this.isDeleted = true;
    }
}