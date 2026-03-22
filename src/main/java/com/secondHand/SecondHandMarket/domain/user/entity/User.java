package com.secondHand.SecondHandMarket.domain.user.entity;

import com.secondHand.SecondHandMarket.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.web.bind.annotation.GetMapping;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 기본 생성자 (외부 직접 호출 방지)
@Builder
@AllArgsConstructor
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Enumerated(EnumType.STRING)      // DB에 "USER", "ADMIN" 문자열로 저장
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.USER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @Column(nullable = false)
    @Builder.Default
    private boolean emailVerified = false;


    // 이메일 인증 완료 처리
    public void verifyEmail() {
        this.emailVerified = true;
    }

    // 회원 정지
    public void ban() {
        this.status = UserStatus.BANNED;
    }

    //정지 해제
    public void activate() {
        this. status = UserStatus.ACTIVE;
    }

    // 닉네임 수정
    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    // 비밀번호 수정
    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

}
