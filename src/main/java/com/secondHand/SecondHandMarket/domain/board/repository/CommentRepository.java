package com.secondHand.SecondHandMarket.domain.board.repository;

import com.secondHand.SecondHandMarket.domain.board.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 게시글의 최상위 댓글만 조회 (parent IS NULL)
    // 대댓글은 각 댓글의 children 컬렉션으로 접근
    //
    // JOIN FETCH 사용 이유:
    //   Comment → User, Comment → Children → User 관계가 모두 LAZY 로딩
    //   N+1 문제 방지: 댓글 100개면 User 조회 쿼리 100번 발생 가능
    //   JOIN FETCH로 한 번의 쿼리에 연관 데이터 모두 로딩
    //
    // isDeleted = false OR SIZE(c.children) > 0 조건:
    //   소프트 삭제된 댓글이더라도 대댓글이 있으면 조회
    //   이유: 부모 댓글 숨기면 대댓글도 화면에서 사라지는 UX 문제 방지
    //   화면에는 "삭제된 댓글입니다."로 표시 (CommentResponse.from에서 처리)
    @Query("""
        SELECT c FROM Comment c
        JOIN FETCH c.user
        LEFT JOIN FETCH c.children ch
        LEFT JOIN FETCH ch.user
        WHERE c.board.id = :boardId
        AND c.parent IS NULL
        AND (c.isDeleted = false OR SIZE(c.children) > 0)
        ORDER BY c.createdAt ASC
        """)
    List<Comment> findByBoardIdWithChildren(@Param("boardId") Long boardId);
}