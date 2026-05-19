package com.secondHand.SecondHandMarket.domain.order.scheduler;

import com.secondHand.SecondHandMarket.domain.order.entity.OrderStatus;
import com.secondHand.SecondHandMarket.domain.order.entity.Orders;
import com.secondHand.SecondHandMarket.domain.order.entity.Payment;
import com.secondHand.SecondHandMarket.domain.product.entity.ProductStatus;
import com.secondHand.SecondHandMarket.domain.order.repository.OrderRepository;
import com.secondHand.SecondHandMarket.domain.order.repository.PaymentRepository;
import com.secondHand.SecondHandMarket.domain.order.service.TossPaymentService;
import com.secondHand.SecondHandMarket.global.exception.CustomException;
import com.secondHand.SecondHandMarket.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentSyncScheduler {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final TossPaymentService tossPaymentService;

    // 5분마다 실행
    @Scheduled(fixedDelay = 300000)
    @Transactional
    public void syncConfirmingOrders() {

        List<Orders> confirmingOrders =
                orderRepository.findByStatus(OrderStatus.CONFIRMING);

        if (confirmingOrders.isEmpty()) return;

        log.warn("[배치] CONFIRMING 상태 주문 {}건 감지 — 동기화 시작",
                confirmingOrders.size());

        for (Orders order : confirmingOrders) {
            try {
                // 토스 API로 실제 결제 상태 조회
                String tossStatus = tossPaymentService.getPaymentStatus(
                        order.getTossOrderId());

                Payment payment = paymentRepository.findByOrderId(order.getId())
                        .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

                if ("DONE".equals(tossStatus)) {
                    // 토스에서 결제 완료 → DB도 PAID로 복구
                    payment.confirm(payment.getPaymentKey());
                    order.paid();
                    order.getProduct().updateStatus(ProductStatus.SOLD);
                    log.info("[배치] 주문 {} PAID 복구 완료", order.getTossOrderId());

                } else if ("ABORTED".equals(tossStatus) || "EXPIRED".equals(tossStatus)) {
                    // 토스에서 실패/만료 → DB도 CANCELLED로 변경
                    payment.refund();
                    order.cancel();
                    order.getProduct().updateStatus(ProductStatus.SALE);
                    log.info("[배치] 주문 {} CANCELLED 처리 완료", order.getTossOrderId());
                }
                // IN_PROGRESS면 아직 처리 중이므로 건너뜀

            } catch (Exception e) {
                log.error("[배치] 주문 {} 동기화 실패: {}", order.getTossOrderId(), e.getMessage());
                // 한 건 실패해도 다음 건 계속 처리
            }
        }
    }
}