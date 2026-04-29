package com.secondHand.SecondHandMarket.global.exception;

import lombok.Getter;

// RuntimeException 상속: 비검사 예외(Unchecked Exception)
// 검사 예외(Checked Exception)는 throws 선언 필요 → 코드가 복잡해짐
// RuntimeException은 throws 선언 없이 자유롭게 던질 수 있어 서비스 레이어에서 사용하기 편함
// GlobalExceptionHandler에서 일괄 처리
@Getter
public class CustomException extends RuntimeException {

    private final ErrorCode errorCode;

    public CustomException(ErrorCode errorCode) {

        // RuntimeException의 message 필드에 에러 메시지 설정
        // → e.getMessage()로 꺼낼 수 있음
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

}
