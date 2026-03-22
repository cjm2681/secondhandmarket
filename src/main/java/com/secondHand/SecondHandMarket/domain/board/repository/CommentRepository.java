package com.secondHand.SecondHandMarket.domain.board.repository;

import com.secondHand.SecondHandMarket.domain.board.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 게시글의 최상위 댓글만 조회 (parent == null)
    // 대댓글은 children으로 접근
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