package com.secondHand.SecondHandMarket.domain.order.service;

import com.secondHand.SecondHandMarket.domain.order.dto.OrderCreateRequest;
import com.secondHand.SecondHandMarket.domain.order.dto.OrderResponse;
import com.secondHand.SecondHandMarket.domain.order.dto.TossPaymentRequest;
import com.secondHand.SecondHandMarket.domain.order.entity.OrderStatus;
import com.secondHand.SecondHandMarket.domain.order.entity.Orders;
import com.secondHand.SecondHandMarket.domain.order.entity.Payment;
import com.secondHand.SecondHandMarket.domain.order.entity.PaymentStatus;
import com.secondHand.SecondHandMarket.domain.order.repository.OrderRepository;
import com.secondHand.SecondHandMarket.domain.order.repository.PaymentRepository;
import com.secondHand.SecondHandMarket.domain.product.entity.Product;
import com.secondHand.SecondHandMarket.domain.product.entity.ProductStatus;
import com.secondHand.SecondHandMarket.domain.product.repository.ProductRepository;
import com.secondHand.SecondHandMarket.domain.user.Repository.UserRepository;
import com.secondHand.SecondHandMarket.domain.user.entity.User;
import com.secondHand.SecondHandMarket.global.exception.CustomException;
import com.secondHand.SecondHandMarket.global.exception.ErrorCode;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    private final TossPaymentService tossPaymentService;

    // 주문 + Mock 결제 생성 (트랜잭션 핵심)
    @Transactional
    public OrderResponse create(Long buyerId, OrderCreateRequest request) {

        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Product product = productRepository.findByIdWithImages(request.getProductId())
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        // 1. 본인 상품 구매 방지
        if (product.getSeller().getId().equals(buyerId)) {
            throw new CustomException(ErrorCode.CANNOT_BUY_OWN_PRODUCT);
        }

        // 2. 판매 중인 상품인지 확인
        if (product.getStatus() != ProductStatus.SALE) {
            throw new CustomException(ErrorCode.PRODUCT_ALREADY_SOLD);
        }

        // 3. 중복 구매 방지
        if (orderRepository.existsByBuyerIdAndProductId(buyerId, product.getId())) {
            throw new CustomException(ErrorCode.ALREADY_PURCHASED);
        }

        // 4. 주문 생성
        Orders order = Orders.builder()
                .buyer(buyer)
                .product(product)
                .totalPrice(product.getPrice())  // 구매 시점 가격 스냅샷
                .build();
        orderRepository.save(order);

        // 5. Mock 결제 처리
        Payment payment = Payment.builder()
                .order(order)
                .paymentKey("MOCK_" + UUID.randomUUID())
                .amount(product.getPrice())
                .build();
        payment.paid();
        paymentRepository.save(payment);

        // 6. 주문 상태 → PAID
        order.paid();

        // 7. 상품 상태 → SOLD (Dirty Checking으로 자동 UPDATE)
        product.updateStatus(ProductStatus.SOLD);

        return OrderResponse.from(order, payment);
    }

    // 내 구매 내역
    public List<OrderResponse> getMyOrders(Long buyerId) {
        return orderRepository.findByBuyerIdWithProduct(buyerId)
                .stream()
                .map(order -> {
                    Payment payment = paymentRepository.findByOrderId(order.getId())
                            .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));
                    return OrderResponse.from(order, payment);
                })
                .toList();
    }

    // 판매자 판매 내역
    public List<OrderResponse> getMySales(Long sellerId) {
        return orderRepository.findBySellerId(sellerId)
                .stream()
                .map(order -> {
                    Payment payment = paymentRepository.findByOrderId(order.getId())
                            .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));
                    return OrderResponse.from(order, payment);
                })
                .toList();
    }

    // 주문 취소
    @Transactional
    public OrderResponse cancel(Long orderId, Long userId) {

        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        // 본인 주문인지 확인
        if (!order.getBuyer().getId().equals(userId)) {
            throw new CustomException(ErrorCode.ORDER_FORBIDDEN);
        }

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

        // 주문 취소 + 환불 처리
        order.cancel();
        payment.refund();

        // 상품 상태 → SALE (재판매 가능)
        order.getProduct().updateStatus(ProductStatus.SALE);

        return OrderResponse.from(order, payment);
    }

    // 주문 상세 조회
    public OrderResponse getDetail(Long orderId, Long userId) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        // 구매자 또는 판매자만 조회 가능
        boolean isBuyer = order.getBuyer().getId().equals(userId);
        boolean isSeller = order.getProduct().getSeller().getId().equals(userId);

        if (!isBuyer && !isSeller) {
            throw new CustomException(ErrorCode.ORDER_FORBIDDEN);
        }

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

        return OrderResponse.from(order, payment);
    }



    // 1단계: 주문 생성 (결제 전, READY 상태)
    @Transactional
    public OrderResponse createReady(Long buyerId, OrderCreateRequest request) {

        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Product product = productRepository.findByIdWithImages(request.getProductId())
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        if (product.getSeller().getId().equals(buyerId)) {
            throw new CustomException(ErrorCode.CANNOT_BUY_OWN_PRODUCT);
        }
        if (product.getStatus() != ProductStatus.SALE) {
            throw new CustomException(ErrorCode.PRODUCT_ALREADY_SOLD);
        }
        if (orderRepository.existsByBuyerIdAndProductIdAndStatus(
                buyerId, product.getId(), OrderStatus.PAID)) {
            throw new CustomException(ErrorCode.ALREADY_PURCHASED);
        }


        // ✅ READY 상태 이전 주문 있으면 Payment 먼저 삭제 후 Order 삭제
        orderRepository.findByBuyerIdAndProductIdAndStatus(
                        buyerId, product.getId(), OrderStatus.READY)
                .ifPresent(oldOrder -> {
                    paymentRepository.findByOrderId(oldOrder.getId())
                            .ifPresent(paymentRepository::delete);
                    orderRepository.delete(oldOrder);
                });

        // ✅ 토스용 고유 주문번호 생성 (UUID 앞 8자 + timestamp)
        String tossOrderId = "ORDER-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase()
                + "-" + System.currentTimeMillis();

        // READY 상태로 주문만 생성 (결제 전)
        Orders order = Orders.builder()
                .buyer(buyer)
                .product(product)
                .totalPrice(product.getPrice())
                .status(OrderStatus.READY)
                .tossOrderId(tossOrderId)   //  추가
                .build();
        orderRepository.save(order);

        // Payment도 READY로 생성
        Payment payment = Payment.builder()
                .order(order)
                .paymentKey("")       // 결제 완료 후 채워짐
                .amount(product.getPrice())
                .status(PaymentStatus.READY)
                .build();
        paymentRepository.save(payment);

        //  상품 상태 RESERVED로 변경 — 여기서 @Version 충돌 발생 가능
        try {
            product.updateStatus(ProductStatus.RESERVED);
        } catch (OptimisticLockException | ObjectOptimisticLockingFailureException e) {
            // 동시에 다른 사람이 먼저 RESERVED로 바꿨을 때
            throw new CustomException(ErrorCode.PRODUCT_CONFLICT);
        }

        return OrderResponse.from(order, payment);
    }




    // 2단계: 토스 결제 승인 + 주문 완료
    @Transactional
    public OrderResponse confirmPayment(Long buyerId, TossPaymentRequest request) {

        // ✅ tossOrderId로 주문 조회
        Orders order = orderRepository.findByTossOrderId(request.getOrderId())
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        // 본인 주문인지 확인
        if (!order.getBuyer().getId().equals(buyerId)) {
            throw new CustomException(ErrorCode.ORDER_FORBIDDEN);
        }

        // 금액 위변조 방지 검증
        if (order.getTotalPrice() != request.getAmount().intValue()) {
            throw new CustomException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        // 토스 서버에 최종 승인 요청
        tossPaymentService.confirm(
                request.getPaymentKey(),
                request.getOrderId(),
                request.getAmount()
        );

        // 승인 성공 → 상태 업데이트
        Payment payment = paymentRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

        payment.confirm(request.getPaymentKey());  // paymentKey 저장 + PAID
        order.paid();
        order.getProduct().updateStatus(ProductStatus.SOLD);

        return OrderResponse.from(order, payment);
    }


}