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
    public void confirm(String paymentKey, String orderId, Long amount) {
        try {
            // 시크릿 키 Base64 인코딩 (토스 인증 방식)
            String encodedKey = Base64.getEncoder()
                    .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));

            HttpClient client = HttpClient.newHttpClient();

            String requestBody = """
                {
                    "paymentKey": "%s",
                    "orderId": "%s",
                    "amount": %d
                }
                """.formatted(paymentKey, orderId, amount);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(TOSS_CONFIRM_URL))
                    .header("Authorization", "Basic " + encodedKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("토스 결제 승인 실패: {}", response.body());
                throw new CustomException(ErrorCode.PAYMENT_CONFIRM_FAILED);
            }

            log.info("토스 결제 승인 성공 - paymentKey: {}", paymentKey);

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("토스 결제 요청 오류: {}", e.getMessage());
            throw new CustomException(ErrorCode.PAYMENT_CONFIRM_FAILED);
        }
    }
}