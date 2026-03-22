package com.secondHand.SecondHandMarket.domain.product.controller;

import com.secondHand.SecondHandMarket.domain.product.dto.*;
import com.secondHand.SecondHandMarket.domain.product.service.ProductService;
import com.secondHand.SecondHandMarket.global.response.ApiResponse;
import com.secondHand.SecondHandMarket.global.util.SecurityUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Tag(name = "Product", description = "판매글 API")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // 판매글 등록 (이미지 포함 - multipart/form-data)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProductResponse>> create(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestPart("data") ProductCreateRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("판매글 등록 성공",
                        productService.create(userId, request, images)));
    }

    // 판매글 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductListResponse>>> getList(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page) {

        return ResponseEntity.ok(ApiResponse.ok(productService.getList(keyword, page)));
    }

    // 판매글 상세 조회
    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse>> getDetail(
            @PathVariable("productId") Long productId,
            HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(productService.getDetail(productId, request)));
    }

    // 판매글 수정
    @PutMapping(value = "/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProductResponse>> update(
            @PathVariable("productId") Long productId,
            @AuthenticationPrincipal Long userId,
            @Valid @RequestPart("data") ProductUpdateRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {

        return ResponseEntity.ok(ApiResponse.ok("수정 성공",
                productService.update(productId, userId, request, images)));
    }

    // 판매글 삭제
    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable("productId") Long productId,
            @AuthenticationPrincipal Long userId) {

        String role = SecurityUtil.getCurrentUserRole();
        productService.delete(productId, userId, role);
        return ResponseEntity.ok(ApiResponse.ok("삭제 성공", null));
    }

    // 내 판매글 목록
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Page<ProductListResponse>>> getMyProducts(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "0") int page) {

        return ResponseEntity.ok(ApiResponse.ok(productService.getMyProducts(userId, page)));
    }


    // 판매글 상태 변경 (판매자 직접)
    @PatchMapping("/{productId}/status")
    public ResponseEntity<ApiResponse<ProductResponse>> updateStatus(
            @PathVariable("productId") Long productId,
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ProductStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("상태 변경 완료",
                productService.updateStatus(productId, userId, request)));
    }




}