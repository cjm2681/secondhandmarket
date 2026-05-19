package com.secondHand.SecondHandMarket.domain.order.service;

import com.secondHand.SecondHandMarket.domain.order.entity.Orders;
import com.secondHand.SecondHandMarket.domain.order.entity.Payment;
import com.secondHand.SecondHandMarket.domain.order.repository.OrderRepository;
import com.secondHand.SecondHandMarket.domain.order.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

    @Service
    @RequiredArgsConstructor
    public class OrderStatusUpdater {

        private final OrderRepository orderRepository;
        private final PaymentRepository paymentRepository;

        // 별도 트랜잭션으로 즉시 커밋 → 부모 트랜잭션 롤백에 영향받지 않음
        @Transactional (propagation = Propagation.REQUIRES_NEW)
        public void markAsConfirming(Orders order, Payment payment) {
            order.confirming();
            payment.confirming();
            orderRepository.save(order);
            paymentRepository.save(payment);
        }
}
