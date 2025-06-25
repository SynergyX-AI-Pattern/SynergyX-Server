package com.synergyx.trading.apiPayload.code.status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import com.synergyx.trading.apiPayload.code.BaseCode;
import com.synergyx.trading.apiPayload.code.ReasonDTO;

@Getter
@AllArgsConstructor
public enum SuccessStatus implements BaseCode {

    _OK(HttpStatus.OK, "COMMON200", "성공입니다."),

    // 알림 관련
    NOTIFICATIONS_DISABLED(HttpStatus.OK, "SUCCESS_NOTIFICATIONS_DISABLED", "알림 수신이 비활성화되어 있습니다."),

    // 관심종목
    SUCCESS_WATCHLIST_REMOVE(HttpStatus.OK, "WATCHLIST200", "관심 종목에서 삭제되었습니다."),
    SUCCESS_WATCHLIST_ADD(HttpStatus.OK, "WATCHLIST200", "관심 종목에 등록되었습니다."),

    // 패턴
    SUCCESS_PATTERN_CREATE(HttpStatus.OK, "PATTERN200", "패턴이 생성되었습니다."),
    SUCCESS_PATTERN_DELETE(HttpStatus.OK, "PATTERN200", "패턴이 삭제되었습니다."),
    SUCCESS_PATTERN_UPDATE(HttpStatus.OK, "PATTERN200", "패턴이 수정되었습니다."),
    SUCCESS_PATTERN_APPLY(HttpStatus.OK, "PATTERN200", "패턴이 적용되었습니다."),
    SUCCESS_PATTERN_NOTIFICATION_TOGGLE(HttpStatus.OK, "PATTERN200", "패턴 알림 설정이 변경되었습니다."),

    // 백테스팅
    SUCCESS_BACKTEST_EXECUTE(HttpStatus.OK, "BACKTEST200", "백테스트가 실행되었습니다."),

    // 종목 상세
    SUCCESS_CHART_DATA(HttpStatus.OK, "CANDLE200", "차트 데이터 조회 성공."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ReasonDTO getReason() {
        return ReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(true)
                .build();
    }

    @Override
    public ReasonDTO getReasonHttpStatus() {
        return ReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(true)
                .httpStatus(httpStatus)
                .build()
                ;
    }
}
