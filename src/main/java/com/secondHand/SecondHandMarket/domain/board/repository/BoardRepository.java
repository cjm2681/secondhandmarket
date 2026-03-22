package com.secondHand.SecondHandMarket.domain.board.repository;

import com.secondHand.SecondHandMarket.domain.board.entity.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long> {

    @Query("""
        SELECT b FROM Board b
        JOIN FETCH b.user
        WHERE b.isDeleted = false
        AND (:keyword IS NULL OR b.title LIKE %:keyword%)
        ORDER BY b.createdAt DESC
        """)
    Page<Board> findAllByKeyword(
            @Param("keyword") String keyword,
            Pageable pageable
    );

    // 상세 조회 - 댓글까지 한 번에
    @Query("""
        SELECT DISTINCT b FROM Board b
        JOIN FETCH b.user
        WHERE b.id = :id AND b.isDeleted = false
        """)
    Optional<Board> findByIdWithUser(@Param("id") Long id);

    Page<Board> findByUserIdAndIsDeletedFalse(Long userId, Pageable pageable);


}