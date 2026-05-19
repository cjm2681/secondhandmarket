package com.secondHand.SecondHandMarket.domain.order.controller;

import com.secondHand.SecondHandMarket.domain.order.dto.TossWebhookRequest;
import com.secondHand.SecondHandMarket.domain.order.entity.OrderStatus;
import com.secondHand.SecondHandMarket.domain.order.entity.Orders;
import com.secondHand.SecondHandMarket.domain.order.entity.Payment;
import com.secondHand.SecondHandMarket.domain.order.repository.OrderRepository;
import com.secondHand.SecondHandMarket.domain.order.repository.PaymentRepository;
import com.secondHand.SecondHandMarket.domain.product.entity.ProductStatus;
import com.secondHand.SecondHandMarket.global.exception.CustomException;
import com.secondHand.SecondHandMarket.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhook")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    @PostMapping("/toss")
    @Transactional
    public ResponseEntity<Void> handleTossWebhook(
            @RequestBody TossWebhookRequest request) {

        log.info("[웹훅] 수신 - eventType: {}, orderId: {}",
                request.getEventType(),
                request.getData().getOrderId());

        // PAYMENT_STATUS_CHANGED 이벤트만 처리
        if (!"PAYMENT_STATUS_CHANGED".equals(request.getEventType())) {
            return ResponseEntity.ok().build();
        }

        String tossOrderId = request.getData().getOrderId();
        String status = request.getData().getStatus();

        Orders order = orderRepository.findByTossOrderId(tossOrderId)
                .orElse(null);

        // 주문 못 찾으면 그냥 200 반환 (토스 재전송 방지)
        if (order == null) {
            log.warn("[웹훅] 주문 없음 - tossOrderId: {}", tossOrderId);
            return ResponseEntity.ok().build();
        }

        // CONFIRMING 상태일 때만 처리 (이미 PAID면 중복 처리 방지)
        if (order.getStatus() != OrderStatus.CONFIRMING) {
            log.info("[웹훅] 이미 처리된 주문 - tossOrderId: {}", tossOrderId);
            return ResponseEntity.ok().build();
        }

        Payment payment = paymentRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

        if ("DONE".equals(status)) {
            payment.confirm(request.getData().getPaymentKey());
            order.paid();
            order.getProduct().updateStatus(ProductStatus.SOLD);
            log.info("[웹훅] 결제 완료 처리 - tossOrderId: {}", tossOrderId);

        } else if ("ABORTED".equals(status) || "EXPIRED".equals(status)) {
            payment.refund();
            order.cancel();
            order.getProduct().updateStatus(ProductStatus.SALE);
            log.info("[웹훅] 결제 실패 처리 - tossOrderId: {}", tossOrderId);
        }

        return ResponseEntity.ok().build();
    }
}