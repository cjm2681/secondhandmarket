package com.secondHand.SecondHandMarket.domain.auth.service;

import com.secondHand.SecondHandMarket.domain.auth.repository.EmailVerificationRedisRepository;
import com.secondHand.SecondHandMarket.domain.user.Repository.UserRepository;
import com.secondHand.SecondHandMarket.global.exception.CustomException;
import com.secondHand.SecondHandMarket.global.exception.ErrorCode;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailVerificationRedisRepository emailVerificationRedisRepository;

    private final UserRepository userRepository;

    // 인증코드 발송
    public void sendVerificationEmail(String email) {
        String code = generateCode();

        // Redis에 저장 (5분 TTL)
        emailVerificationRedisRepository.save(email, code);

        // 이메일 발송
        send(email, "[SecondHandMarket] 이메일 인증코드", buildEmailContent(code));

        log.info("인증코드 발송 완료 - email: {}", email);
    }

    // 인증코드 검증
    public void verifyCode(String email, String code) {
        String savedCode = emailVerificationRedisRepository.find(email)
                .orElseThrow(() -> new CustomException(ErrorCode.VERIFICATION_CODE_NOT_FOUND));

        if (!savedCode.equals(code)) {
            throw new CustomException(ErrorCode.VERIFICATION_CODE_MISMATCH);
        }

        // 검증 성공 시 Redis에서 삭제
        emailVerificationRedisRepository.delete(email);
        emailVerificationRedisRepository.saveVerified(email);  //  인증 완료 표시
    }

    // 6자리 랜덤 코드 생성
    private String generateCode() {
        return String.format("%06d", new Random().nextInt(1000000));
    }

    private void send(String to, String subject, String content) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);  // true = HTML 형식
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new CustomException(ErrorCode.EMAIL_SEND_FAILED);
        }
    }

    // 이메일 HTML 내용
    private String buildEmailContent(String code) {
        return """
                <div style="font-family: Arial, sans-serif; max-width: 500px; margin: 0 auto;">
                    <h2 style="color: #333;">이메일 인증코드</h2>
                    <p>아래 인증코드를 입력해주세요. <strong>5분</strong> 안에 입력해야 합니다.</p>
                    <div style="background: #f5f5f5; padding: 20px; text-align: center;
                                font-size: 32px; font-weight: bold; letter-spacing: 8px;
                                border-radius: 8px; color: #4A90E2;">
                        %s
                    </div>
                </div>
                """.formatted(code);
    }


    // 비밀번호 찾기, 재설정 메일 부분
    public void sendPasswordResetEmail(String email) {
        // 가입된 이메일인지 확인
        if (!userRepository.existsByEmail(email)) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        String code = generateCode();
        emailVerificationRedisRepository.saveResetCode(email, code);
        send(email, "[SecondHandMarket] 비밀번호 재설정 코드", buildResetEmailContent(code));
    }

    public void verifyResetCode(String email, String code) {
        String savedCode = emailVerificationRedisRepository.findResetCode(email)
                .orElseThrow(() -> new CustomException(ErrorCode.VERIFICATION_CODE_NOT_FOUND));

        if (!savedCode.equals(code)) {
            throw new CustomException(ErrorCode.VERIFICATION_CODE_MISMATCH);
        }
        // 코드 유지 (비밀번호 변경 시까지 유효)
    }

    private String buildResetEmailContent(String code) {
        return """
            <div style="font-family: Arial, sans-serif; max-width: 500px; margin: 0 auto;">
                <h2 style="color: #333;">비밀번호 재설정</h2>
                <p>아래 코드를 입력하고 새 비밀번호를 설정해주세요. <strong>10분</strong> 안에 입력해야 합니다.</p>
                <div style="background: #f5f5f5; padding: 20px; text-align: center;
                            font-size: 32px; font-weight: bold; letter-spacing: 8px;
                            border-radius: 8px; color: #E24B4A;">
                    %s
                </div>
            </div>
            """.formatted(code);
    }

}