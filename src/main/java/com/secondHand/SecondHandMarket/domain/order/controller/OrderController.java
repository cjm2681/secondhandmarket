package com.secondHand.SecondHandMarket.domain.order.controller;

import com.secondHand.SecondHandMarket.domain.order.dto.OrderCreateRequest;
import com.secondHand.SecondHandMarket.domain.order.dto.OrderResponse;
import com.secondHand.SecondHandMarket.domain.order.dto.TossPaymentRequest;
import com.secondHand.SecondHandMarket.domain.order.service.OrderService;
import com.secondHand.SecondHandMarket.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Tag(name = "Order", description = "주문/결제 API")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // 주문 + 결제 생성
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> create(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody OrderCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("주문 완료", orderService.create(userId, request)));
    }

    // 내 구매 내역
    @GetMapping("/my-purchases")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getMyOrders(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getMyOrders(userId)));
    }

    // 내 판매 내역
    @GetMapping("/my-sales")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getMySales(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getMySales(userId)));
    }

    // 주문 상세 조회
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getDetail(
            @PathVariable("orderId") Long orderId,
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getDetail(orderId, userId)));
    }

    // 주문 취소
    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancel(
            @PathVariable("orderId") Long orderId,
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.ok("주문 취소 완료", orderService.cancel(orderId, userId)));
    }



    // 1단계: 주문 생성 (결제창 띄우기 전)
    @PostMapping("/ready")
    public ResponseEntity<ApiResponse<OrderResponse>> createReady(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody OrderCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("주문 생성 완료",
                        orderService.createReady(userId, request)));
    }

    // 2단계: 결제 승인 (토스에서 콜백 후 호출)
    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<OrderResponse>> confirmPayment(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody TossPaymentRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("결제 완료",
                orderService.confirmPayment(userId, request)));
    }



}