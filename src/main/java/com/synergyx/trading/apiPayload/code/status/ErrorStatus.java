package com.synergyx.trading.apiPayload.code.status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import com.synergyx.trading.apiPayload.code.BaseErrorCode;
import com.synergyx.trading.apiPayload.code.ErrorReasonDTO;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorCode {

    _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러, 관리자에게 문의 바랍니다."),
    _BAD_REQUEST(HttpStatus.BAD_REQUEST,"COMMON400","잘못된 요청입니다."),
    _BAD_REQUEST_SAME_STATE(HttpStatus.BAD_REQUEST, "COMMON4002", "수정하려는 데이터가 현재 상태와 동일합니다."),
    _UNAUTHORIZED(HttpStatus.UNAUTHORIZED,"COMMON401","인증이 필요합니다."),
    _FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "금지된 요청입니다."),

    // 사용자 관련 예외 처리
    USER_NOT_FOUND(HttpStatus.BAD_REQUEST, "USER404", "해당 ID를 가진 사용자를 찾을 수 없습니다."),

    // 관심종목 관련 예외 처리
    ALREADY_REGISTERED_INTEREST(HttpStatus.BAD_REQUEST, "INTEREST4001", "이미 관심 종목에 등록되어 있습니다."),
    INTEREST_NOT_FOUND(HttpStatus.NOT_FOUND, "INTEREST404", "관심 종목이 존재하지 않습니다."),

    // 종목 관련 예외 처리
    STOCK_NOT_FOUND(HttpStatus.NOT_FOUND, "STOCK404", "해당 종목을 찾을 수 없습니다."),

    // 패턴 관련 예외 처리
    PATTERN_NOT_FOUND(HttpStatus.NOT_FOUND, "PATTERN404", "패턴이 존재하지 않습니다."),

    // 패턴 적용 관련 예외 처리
    PATTERN_APPLY_NOT_FOUND(HttpStatus.NOT_FOUND, "PATTERN_APPLY404", "해당 패턴 적용 정보가 존재하지 않습니다."),

    // 백테스팅 관련 예외 처리
    BACKTEST_NOT_FOUND(HttpStatus.NOT_FOUND, "BACKTEST404", "해당 백테스트 결과가 존재하지 않습니다."),
    BACKTEST_PERIOD_EXCEEDS_LIMIT(HttpStatus.BAD_REQUEST, "BACKTEST400", "백테스트 기간은 최대 5년까지 가능합니다."),
    FASTAPI_BACKTEST_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "BACKTEST502", "FastAPI 백테스트 서버 호출에 실패했습니다."),

    // 캔들 데이터 관련 예외 처리
    CANDLE_DATA_NOT_FOUND(HttpStatus.NOT_FOUND, "CANDLE404", "캔들 데이터가 존재하지 않습니다."),
    INVALID_CANDLE_INTERVAL(HttpStatus.BAD_REQUEST, "CANDLE4001", "유효하지 않은 캔들 조회 주기입니다."),

    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDTO getReason() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .build();
    }

    @Override
    public ErrorReasonDTO getReasonHttpStatus() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .httpStatus(httpStatus)
                .build()
                ;
    }
}
