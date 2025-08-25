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
    _BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON400", "잘못된 요청입니다."),
    _BAD_REQUEST_SAME_STATE(HttpStatus.BAD_REQUEST, "COMMON4002", "수정하려는 데이터가 현재 상태와 동일합니다."),
    _UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON401", "인증이 필요합니다."),
    _FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "금지된 요청입니다."),

    // 사용자 관련 예외 처리
    USER_NOT_FOUND(HttpStatus.BAD_REQUEST, "USER404", "해당 ID를 가진 사용자를 찾을 수 없습니다."),

    // 관심종목 관련 예외 처리
    ALREADY_REGISTERED_INTEREST(HttpStatus.BAD_REQUEST, "INTEREST4001", "이미 관심 종목에 등록되어 있습니다."),
    INTEREST_NOT_FOUND(HttpStatus.NOT_FOUND, "INTEREST404", "관심 종목이 존재하지 않습니다."),

    // 종목 관련 예외 처리
    STOCK_NOT_FOUND(HttpStatus.NOT_FOUND, "STOCK404", "해당 종목을 찾을 수 없습니다."),
    STOCK_DETAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "STOCK_DETAIL404", "해당 종목 상세 정보를 찾을 수 없습니다."),
    IMAGE_STOCK_NOT_FOUND(HttpStatus.NOT_FOUND, "STOCK_IMAGE404", "이미지로 종목을 찾을 수 없습니다."),
    STOCK_PREDICTION_NOT_FOUND(HttpStatus.NOT_FOUND, "STOCK4041", "종목의 예측 데이터를 찾을 수 없습니다."),
    STOCK_CLOSE_NOT_FOUND(HttpStatus.NOT_FOUND, "STOCK4042", "해당 종목의 종가를 찾을 수 없습니다."),

    // 패턴 관련 예외 처리
    PATTERN_NOT_FOUND(HttpStatus.NOT_FOUND, "PATTERN404", "패턴이 존재하지 않습니다."),
    INVALID_PATTERN_DURATION(HttpStatus.BAD_REQUEST, "PATTERN4001", "유효하지 않은 패턴 기간입니다."),

    // 패턴 적용 관련 예외 처리
    PATTERN_APPLY_NOT_FOUND(HttpStatus.NOT_FOUND, "PATTERN_APPLY404", "해당 패턴 적용 정보가 존재하지 않습니다."),
    INVALID_ENTRY_AT(HttpStatus.BAD_REQUEST, "PATTERN_APPLY400", "감지 시작일은 현재보다 미래일 수 없습니다."),
    INVALID_MIN_VALID_RETURN(HttpStatus.BAD_REQUEST, "PATTERN_APPLY4002", "최소 유효 수익률은 0 이상이어야 합니다."),

    // 백테스팅 관련 예외 처리
    BACKTEST_NOT_FOUND(HttpStatus.NOT_FOUND, "BACKTEST404", "해당 백테스트 결과가 존재하지 않습니다."),
    BACKTEST_PERIOD_EXCEEDS_LIMIT(HttpStatus.BAD_REQUEST, "BACKTEST400", "백테스트 기간은 최대 5년까지 가능합니다."),
    FASTAPI_BACKTEST_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "BACKTEST502", "FastAPI 백테스트 서버 호출에 실패했습니다."),

    // 캔들 데이터 관련 예외 처리
    CANDLE_DATA_NOT_FOUND(HttpStatus.NOT_FOUND, "CANDLE404", "캔들 데이터가 존재하지 않습니다."),
    INVALID_CANDLE_INTERVAL(HttpStatus.BAD_REQUEST, "CANDLE4001", "유효하지 않은 캔들 조회 주기입니다."),

    // 알림 데이터 관련 예외 처리
    INVALID_FCM_TOKEN(HttpStatus.BAD_REQUEST, "NOTIFICATION401", "유효하지 않은 FCM 토큰입니다."),
    FCM_TOKEN_NOT_FOUND(HttpStatus.BAD_REQUEST, "NOTIFICATION400", "FCM 토큰이 존재하지 않아 알림을 전송할 수 없습니다."),
    INVALID_NOTIFICATION_TYPE(HttpStatus.BAD_REQUEST, "NOTIFICATION4001", "유효하지 않은 알림 타입입니다."),

    // 감정 투자 일기 관련 예외 처리
    INVALID_CONTENT(HttpStatus.BAD_REQUEST, "DIARY400", "일기 내용이 비어있습니다."),
    FASTAPI_ANALYSIS_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "DIARY502", "FastAPI 감정 분석 서버 호출에 실패했습니다."),
    DIARY_NOT_FOUND(HttpStatus.NOT_FOUND, "DIARY404", "해당 일기를 찾을 수 없습니다."),

    // 이미지 관련 예외 처리
    IMAGE_FILE_MISSING(HttpStatus.BAD_REQUEST, "IMAGE4001", "업로드된 이미지가 없습니다."),
    IMAGE_FILE_TOO_LARGE(HttpStatus.BAD_REQUEST, "IMAGE4002", "업로드 가능한 최대 용량은 5MB입니다."),
    INVALID_IMAGE_FILE_TYPE(HttpStatus.BAD_REQUEST, "IMAGE4003", "지원하지 않는 이미지 형식입니다."),
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
