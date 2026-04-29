package com.secondHand.SecondHandMarket.domain.auth.service;

import com.secondHand.SecondHandMarket.domain.auth.dto.LoginRequest;
import com.secondHand.SecondHandMarket.domain.auth.dto.PasswordResetRequest;
import com.secondHand.SecondHandMarket.domain.auth.dto.TokenResponse;
import com.secondHand.SecondHandMarket.domain.auth.repository.EmailVerificationRedisRepository;
import com.secondHand.SecondHandMarket.domain.user.Repository.UserRepository;
import com.secondHand.SecondHandMarket.domain.user.entity.User;
import com.secondHand.SecondHandMarket.domain.user.entity.UserStatus;
import com.secondHand.SecondHandMarket.global.exception.CustomException;
import com.secondHand.SecondHandMarket.global.exception.ErrorCode;
import com.secondHand.SecondHandMarket.global.jwt.JwtProvider;
import com.secondHand.SecondHandMarket.global.jwt.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    private final EmailService emailService;
    private final EmailVerificationRedisRepository emailVerificationRedisRepository;

    // 로그인
    @Transactional
    public TokenResponse login(LoginRequest request) {

        // 1. 이메일로 유저 조회
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. 정지 계정 체크 → 정지 유저는 토큰 발급 자체를 막음
        if (user.getStatus() == UserStatus.BANNED) {
            throw new CustomException(ErrorCode.BANNED_USER);
        }

        // 3. 비밀번호 검증
        // passwordEncoder.matches(rawPassword, encodedPassword)
        // BCrypt는 같은 값이어도 salt 때문에 매번 해시 결과가 다름 → == 비교 불가, matches 필수
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        // 4. 토큰 발급
        // Access Token: 짧은 유효기간 (실제 API 호출에 사용)
        // Refresh Token: 긴 유효기간 (Access Token 재발급용)
        String accessToken = jwtProvider.createAccessToken(user.getId(), user.getRole().name());
        String refreshToken = jwtProvider.createRefreshToken(user.getId());

        // 5. Redis에 Refresh Token 저장
        // 저장 이유: 로그아웃 시 서버에서 토큰을 무효화하기 위해
        // JWT는 stateless라 서버가 토큰을 기억하지 않음
        // → Redis에 저장해두고 로그아웃 시 삭제 → 해당 Refresh Token으로 재발급 차단
        refreshTokenRepository.save(user.getId(), refreshToken);

        return TokenResponse.of(accessToken, refreshToken);
    }

    // Access Token 재발급
    @Transactional
    public TokenResponse reissue(String refreshToken) {

        // 1. Refresh Token 유효성 검증
        jwtProvider.validateToken(refreshToken);
        Long userId = jwtProvider.getUserId(refreshToken);

        // 2. Redis에 저장된 토큰과 비교
        // 이유: 로그아웃한 유저의 Refresh Token으로 재발급 시도를 막기 위함
        // 로그아웃 시 Redis에서 삭제했으므로 find() 결과가 없음 → 예외 발생
        String savedToken = refreshTokenRepository.find(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

        if (!savedToken.equals(refreshToken)) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_MISMATCH);
        }

        // 3. 새 Access Token만 발급 (Refresh Token은 재사용)
        // Refresh Token 갱신 정책에 따라 다르지만 현재는 Refresh Token은 유지
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String newAccessToken = jwtProvider.createAccessToken(user.getId(), user.getRole().name());

        return TokenResponse.of(newAccessToken, refreshToken);  // Refresh Token 재사용
    }

    // 로그아웃
    @Transactional
    public void logout(Long userId) {
        refreshTokenRepository.delete(userId);
    }


    @Transactional
    public void resetPassword(PasswordResetRequest request) {
        // 1. 코드 검증
        emailService.verifyResetCode(request.getEmail(), request.getCode());

        // 2. 유저 조회 후 비밀번호 변경
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));

        // 3. 사용한 코드 삭제
        emailVerificationRedisRepository.deleteResetCode(request.getEmail());
    }





}