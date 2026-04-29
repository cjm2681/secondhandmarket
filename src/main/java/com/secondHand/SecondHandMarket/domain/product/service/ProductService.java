package com.secondHand.SecondHandMarket.domain.product.service;

import com.secondHand.SecondHandMarket.domain.product.dto.*;
import com.secondHand.SecondHandMarket.domain.product.entity.Product;
import com.secondHand.SecondHandMarket.domain.product.entity.ProductImage;
import com.secondHand.SecondHandMarket.domain.product.entity.ProductStatus;
import com.secondHand.SecondHandMarket.domain.product.repository.ProductImageRepository;
import com.secondHand.SecondHandMarket.domain.product.repository.ProductRepository;
import com.secondHand.SecondHandMarket.domain.user.Repository.UserRepository;
import com.secondHand.SecondHandMarket.domain.user.entity.User;
import com.secondHand.SecondHandMarket.global.exception.CustomException;
import com.secondHand.SecondHandMarket.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final UserRepository userRepository;
    private final ImageService imageService;

    private final RedisTemplate<String, String> redisTemplate;

    // 판매글 등록
    @Transactional
    public ProductResponse create(Long sellerId,
                                  ProductCreateRequest request,
                                  List<MultipartFile> images) {

        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Product product = Product.builder()
                .seller(seller)
                .title(request.getTitle())
                .description(request.getDescription())
                .price(request.getPrice())
                .build();

        // 이미지 업로드 후 연결
        List<String> imageUrls = imageService.uploadImages(images);
        for (int i = 0; i < imageUrls.size(); i++) {
            ProductImage image = ProductImage.builder()
                    .imageUrl(imageUrls.get(i))
                    .sortOrder(i)
                    .build();
            product.addImage(image);
        }

        return ProductResponse.from(productRepository.save(product));
    }

    // 판매글 목록 조회 (페이징 + 검색)
    public Page<ProductListResponse> getList(String keyword, int page) {
        Pageable pageable = PageRequest.of(page, 10);  // 한 페이지 12개
        return productRepository
                .findAllByKeyword(keyword, pageable)
                .map(ProductListResponse::from);
    }

    // 판매글 상세 조회
    @Transactional
    public ProductResponse getDetail( Long productId, HttpServletRequest request) {
        Product product = productRepository.findByIdWithImages(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        String viewer = getViewerIdentifier(request);

        long start = System.currentTimeMillis();

        String key = "VIEW:PRODUCT" + productId + ":" + viewer;

        if (!Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            product.increaseViewCount();
            redisTemplate.opsForValue().set(key, "1", 1L, TimeUnit.HOURS);

            long end = System.currentTimeMillis();
            log.info("[조회수 처리 시간 - Redis방식] 처리시간: {}ms", end - start);

        }

        return ProductResponse.from(product);
    }


    // 로그인 유저면 "USER:1", 비로그인이면 "IP:127.0.0.1" 반환
    private String getViewerIdentifier(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null
                && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof Long userId) {
            return "USER:" + userId;
        }

        // 비로그인 → IP (프록시 환경 고려)
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) {
            ip = request.getRemoteAddr();
        }
        return "IP:" + ip;
    }


    // 판매글 수정
    @Transactional
    public ProductResponse update(Long productId,
                                  Long requestUserId,
                                  ProductUpdateRequest request,
                                  List<MultipartFile> newImages) {

        Product product = getProductOrThrow(productId);
        checkOwnership(product, requestUserId);   // 본인 글인지 확인

        product.update(request.getTitle(), request.getDescription(), request.getPrice());

        // 새 이미지가 있으면 기존 이미지 삭제 후 교체
        if (newImages != null && !newImages.isEmpty()) {
            product.getImages().forEach(img -> imageService.deleteImage(img.getImageUrl()));
            product.getImages().clear();  // orphanRemoval로 DB에서도 삭제

            List<String> imageUrls = imageService.uploadImages(newImages);
            for (int i = 0; i < imageUrls.size(); i++) {
                product.addImage(ProductImage.builder()
                        .imageUrl(imageUrls.get(i))
                        .sortOrder(i)
                        .build());
            }
        }

        return ProductResponse.from(product);
    }

    // 판매글 삭제 (소프트 삭제)
    @Transactional
    public void delete(Long productId, Long requestUserId, String role) {
        Product product = getProductOrThrow(productId);

        // 본인 또는 어드민만 삭제 가능
        if (!product.getSeller().getId().equals(requestUserId) && !role.equals("ADMIN")) {
            throw new CustomException(ErrorCode.PRODUCT_FORBIDDEN);
        }

        product.delete();  // is_deleted = true
    }

    // 내 판매글 목록
    public Page<ProductListResponse> getMyProducts(Long sellerId, int page) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by("createdAt").descending());
        return productRepository
                .findBySellerIdAndIsDeletedFalse(sellerId, pageable)
                .map(ProductListResponse::from);
    }

    // 공통 - 상품 조회
    private Product getProductOrThrow(Long productId) {
        return productRepository.findById(productId)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    // 공통 - 본인 글 확인
    private void checkOwnership(Product product, Long userId) {
        if (!product.getSeller().getId().equals(userId)) {
            throw new CustomException(ErrorCode.PRODUCT_FORBIDDEN);
        }
    }

    //  주문 상태 변경 SALE → RESERVED → SALE 등
    @Transactional
    public ProductResponse updateStatus(Long productId, Long userId, ProductStatusRequest request) {
        Product product = getProductOrThrow(productId);
        checkOwnership(product, userId);  // 본인 상품만 변경 가능

        // SOLD는 직접 변경 불가 (주문을 통해서만 변경)
        if (request.getStatus() == ProductStatus.SOLD) {
            throw new CustomException(ErrorCode.CANNOT_SET_SOLD_DIRECTLY);
        }

        product.updateStatus(request.getStatus());
        return ProductResponse.from(product);
    }






}