package com.secondHand.SecondHandMarket.global.response;

import lombok.Builder;
import lombok.Getter;


// 모든 API 응답을 통일된 형식으로 감싸는 래퍼 클래스
// 이점:
//   1. 클라이언트가 응답 구조를 예측 가능 → 파싱 로직 단순화
//   2. success 필드로 성공/실패 여부를 HTTP 상태코드 외에 추가로 전달
//   3. 제네릭 <T>로 data 타입을 유연하게 지원
//
// 실제 응답 예시:
// 성공: { "success": true, "message": "회원가입 성공", "data": { "id": 1, ... } }
// 실패: { "success": false, "message": "이미 사용 중인 이메일입니다", "data": null }
@Getter
@Builder
public class ApiResponse<T> {

    private boolean success;    // 성공 여부 (true/false)
    private String message;     // 응답 메시지
    private T data;              // 실제 응답 데이터 (실패 시 null)


    // 데이터만 반환할 때 (메시지 없음)
    // 사용: 목록 조회, 단건 조회 등
    // 예: ApiResponse.ok(productList)
    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("success")
                .data(data)
                .build();
    }

    // 메시지 + 데이터 함께 반환할 때
    // 사용: 생성, 수정, 삭제 등 명시적 메시지가 필요한 경우
    // 예: ApiResponse.ok("회원가입 성공", userResponse)
    public static <T> ApiResponse<T> ok(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    // 실패 응답 (data는 항상 null)
    // GlobalExceptionHandler에서 사용
    // 예: ApiResponse.fail("존재하지 않는 판매글입니다")
    public static <T> ApiResponse<T> fail(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .data(null)
                .build();
    }

}
