package com.secondHand.SecondHandMarket.domain.order.service;

import com.secondHand.SecondHandMarket.global.exception.CustomException;
import com.secondHand.SecondHandMarket.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
@Slf4j
public class TossPaymentService {

    @Value("${toss payments.secret-key}")
    private String secretKey;

    private static final String TOSS_CONFIRM_URL =
            "https://api.tosspayments.com/v1/payments/confirm";

    // 토스 결제 최종 승인 요청
    // 이 메서드가 성공하면 실제 돈이 빠져나감 → 이전에 금액 위변조 검증 필수
    public void confirm(String paymentKey, String orderId, Long amount) {
        try {
            // 토스페이먼츠 Basic Auth 인증 방식
            // HTTP Basic Authentication: Base64(username:password) 형태
            // 토스는 비밀번호 없이 secretKey만 사용 → secretKey + ":" 뒤에 콜론만 붙임
            // ":"는 username:password 구분자인데 password가 없으므로 콜론만 남음
            // getBytes(UTF-8): 어떤 OS 환경에서도 동일한 바이트 배열 보장 (기본값은 JVM에 따라 다름)
            String encodedKey = Base64.getEncoder()
                    .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));

            HttpClient client = HttpClient.newHttpClient();

            // Java 15+ Text Block: 여러 줄 문자열을 가독성 있게 작성
            // .formatted(): String.format()의 인스턴스 메서드 버전
            String requestBody = """
                {
                    "paymentKey": "%s",
                    "orderId": "%s",
                    "amount": %d
                }
                """.formatted(paymentKey, orderId, amount);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(TOSS_CONFIRM_URL))
                    .header("Authorization", "Basic " + encodedKey)     // Basic Auth 헤더
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());

            // 토스 응답 200이 아니면 결제 실패 (400: 잘못된 요청, 500: 토스 서버 오류 등)
            if (response.statusCode() != 200) {
                log.error("토스 결제 승인 실패: {}", response.body());
                throw new CustomException(ErrorCode.PAYMENT_CONFIRM_FAILED);
            }

            log.info("토스 결제 승인 성공 - paymentKey: {}", paymentKey);

        } catch (CustomException e) {
            throw e;    // CustomException은 그대로 재던짐 (다른 예외로 감싸지 않기 위해)
        } catch (Exception e) {
            // IOException (네트워크 오류), InterruptedException 등 → 동일하게 결제 실패 처리
            log.error("토스 결제 요청 오류: {}", e.getMessage());
            throw new CustomException(ErrorCode.PAYMENT_CONFIRM_FAILED);
        }
    }
}