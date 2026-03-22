package com.secondHand.SecondHandMarket.domain.user.Repository;

import com.secondHand.SecondHandMarket.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByNickname(String nickname);
    
    boolean existsByEmail(String email);    // 이메일 중복 체크
    
    boolean existsByNickname(String nickname);  // 닉네임 중복 체크


    @Query("""
    SELECT u FROM User u
    WHERE (:keyword IS NULL
        OR u.nickname LIKE %:keyword%
        OR u.email LIKE %:keyword%)
    ORDER BY u.createdAt DESC
    """)
    Page<User> findAllByKeyword(
            @Param("keyword") String keyword,
            Pageable pageable
    );


}
