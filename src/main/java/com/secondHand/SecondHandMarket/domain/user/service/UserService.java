package com.secondHand.SecondHandMarket.domain.user.service;

import com.secondHand.SecondHandMarket.domain.auth.repository.EmailVerificationRedisRepository;
import com.secondHand.SecondHandMarket.domain.user.Repository.UserRepository;
import com.secondHand.SecondHandMarket.domain.user.dto.SignupRequest;
import com.secondHand.SecondHandMarket.domain.user.dto.UpdatePasswordRequest;
import com.secondHand.SecondHandMarket.domain.user.dto.UpdateProfileRequest;
import com.secondHand.SecondHandMarket.domain.user.dto.UserResponse;
import com.secondHand.SecondHandMarket.domain.user.entity.User;
import com.secondHand.SecondHandMarket.global.exception.CustomException;
import com.secondHand.SecondHandMarket.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)  // 기본은 readOnly, 변경이 필요한 메서드만 @Transactional 따로
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationRedisRepository emailVerificationRedisRepository;

    @Transactional
    public UserResponse signup(SignupRequest request) {

        // 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }

        // 이메일 인증 여부 확인 (Redis에 인증 완료 기록 확인)
        if (!emailVerificationRedisRepository.isVerified(request.getEmail())) {
            throw new CustomException(ErrorCode.EMAIL_NOT_VERIFIED);
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // BCrypt 암호화
                .nickname(request.getNickname())
                .build();

        user.verifyEmail();

        //  회원가입 완료 후 Redis 인증 완료 키 삭제
        emailVerificationRedisRepository.deleteVerified(request.getEmail());

        return UserResponse.from(userRepository.save(user));
    }


    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (request.getNickname() != null) {
            // 닉네임 중복 체크 (본인 닉네임은 제외)
            if (!user.getNickname().equals(request.getNickname())
                    && userRepository.existsByNickname(request.getNickname())) {
                throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
            }
            user.updateNickname(request.getNickname());
        }

        return UserResponse.from(user);
    }

    @Transactional
    public void updatePassword(Long userId, UpdatePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 현재 비밀번호 검증
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        // 현재 비밀번호와 새 비밀번호가 같으면 거부
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.SAME_AS_CURRENT_PASSWORD);
        }

        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
    }





    public UserResponse getUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return UserResponse.from(user);
    }
}