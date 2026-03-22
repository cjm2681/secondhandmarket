package com.secondHand.SecondHandMarket.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // User
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다"),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다"),
    BANNED_USER(HttpStatus.FORBIDDEN, "정지된 계정입니다"),

    // Auth
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 올바르지 않습니다"),
    EMAIL_NOT_VERIFIED(HttpStatus.FORBIDDEN, "이메일 인증이 필요합니다"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다"),
    SAME_AS_CURRENT_PASSWORD(HttpStatus.BAD_REQUEST, "현재 비밀번호와 동일합니다"),

    // jwt 토큰
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다"),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "Refresh Token이 존재하지 않습니다"),
    REFRESH_TOKEN_MISMATCH(HttpStatus.UNAUTHORIZED, "Refresh Token이 일치하지 않습니다"),

    
    // 이메일 인증
    VERIFICATION_CODE_NOT_FOUND(HttpStatus.NOT_FOUND, "인증코드가 존재하지 않습니다. 다시 요청해주세요"),
    VERIFICATION_CODE_MISMATCH(HttpStatus.BAD_REQUEST, "인증코드가 일치하지 않습니다"),
    EMAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이메일 발송에 실패했습니다"),
    ALREADY_VERIFIED_EMAIL(HttpStatus.BAD_REQUEST, "이미 인증된 이메일입니다"),



    //판매글
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 판매글입니다"),
    PRODUCT_FORBIDDEN(HttpStatus.FORBIDDEN, "수정/삭제 권한이 없습니다"),
    IMAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 업로드에 실패했습니다"),
    EMPTY_FILE(HttpStatus.BAD_REQUEST, "파일이 비어있습니다"),
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "이미지 파일만 업로드 가능합니다"),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "파일 크기는 10MB 이하여야 합니다"),
    CANNOT_SET_SOLD_DIRECTLY(HttpStatus.BAD_REQUEST, "판매완료는 직접 변경할 수 없습니다"),

    // 자유게시판, 댓글
    BOARD_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 게시글입니다"),
    BOARD_FORBIDDEN(HttpStatus.FORBIDDEN, "수정/삭제 권한이 없습니다"),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 댓글입니다"),
    COMMENT_FORBIDDEN(HttpStatus.FORBIDDEN, "댓글 수정/삭제 권한이 없습니다"),
    PARENT_COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "부모 댓글이 존재하지 않습니다"),
    REPLY_DEPTH_EXCEEDED(HttpStatus.BAD_REQUEST, "대댓글에는 댓글을 달 수 없습니다"),

    // 주문, 결제
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 주문입니다"),
    ORDER_FORBIDDEN(HttpStatus.FORBIDDEN, "주문 조회 권한이 없습니다"),
    ORDER_CANNOT_CANCEL(HttpStatus.BAD_REQUEST, "결제 완료된 주문만 취소할 수 있습니다"),
    PRODUCT_ALREADY_SOLD(HttpStatus.BAD_REQUEST, "이미 판매된 상품입니다"),
    CANNOT_BUY_OWN_PRODUCT(HttpStatus.BAD_REQUEST, "본인 상품은 구매할 수 없습니다"),
    ALREADY_PURCHASED(HttpStatus.BAD_REQUEST, "이미 구매한 상품이거나 취소한 상품입니다"),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "결제 정보가 존재하지 않습니다"),


    // 채팅
    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 채팅방입니다"),
    CHAT_ROOM_FORBIDDEN(HttpStatus.FORBIDDEN, "채팅방 접근 권한이 없습니다"),
    CANNOT_CHAT_OWN_PRODUCT(HttpStatus.BAD_REQUEST, "본인 상품에는 채팅을 할 수 없습니다"),


    CANNOT_BAN_ADMIN(HttpStatus.BAD_REQUEST, "관리자 계정은 정지할 수 없습니다"),


    //  토스 페이먼츠
    PAYMENT_CONFIRM_FAILED(HttpStatus.BAD_REQUEST, "결제 승인에 실패했습니다"),
    PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "결제 금액이 일치하지 않습니다");

    private final HttpStatus httpStatus;
    private final String message;
}
