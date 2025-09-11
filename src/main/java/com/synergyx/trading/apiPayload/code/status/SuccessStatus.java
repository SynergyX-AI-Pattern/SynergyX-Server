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

    // 패턴-종목 적용
    SUCCESS_PATTERN_APPLY(HttpStatus.OK, "PATTERN200", "패턴이 적용되었습니다."),
    SUCCESS_PATTERN_NOTIFICATION_TOGGLE(HttpStatus.OK, "PATTERN200", "패턴 알림 설정이 변경되었습니다."),
    SUCCESS_PATTERN_APPLY_UPDATE(HttpStatus.OK, "PATTERN200", "패턴 적용 정보가 수정되었습니다."),
    SUCCESS_PATTERN_APPLY_UNLINK(HttpStatus.OK, "PATTERN200", "패턴 적용이 해제되었습니다."),
    SUCCESS_PATTERN_APPLY_DETAIL(HttpStatus.OK,"PATTERN200", "종목-패턴 적용 상세 조회 성공"),

    // 백테스팅
    SUCCESS_BACKTEST_EXECUTE(HttpStatus.OK, "BACKTEST200", "백테스트가 실행되었습니다."),

    // 종목 상세
    SUCCESS_CHART_DATA(HttpStatus.OK, "CANDLE200", "차트 데이터 조회 성공."),

    // 알림
    SUCCESS_NOTIFICATION_SENT(HttpStatus.OK, "NOTIFICATION200", "알림이 전송되었습니다."),
    SUCCESS_NOTIFICATION_READ(HttpStatus.OK, "NOTIFICATION200", "알림을 읽음 처리했습니다."),
    SUCCESS_NOTIFICATION_DELETED(HttpStatus.OK, "NOTIFICATION200", "알림이 삭제되었습니다."),

    // 사용자
    SUCCESS_FCM_TOKEN_UPDATED(HttpStatus.OK, "FCM200", "FCM 토큰이 성공적으로 저장되었습니다."),

    // 감정 투자 일기
    SUCCESS_WRITE_EMOTION_DIARY(HttpStatus.OK, "DIARY200", "감정 투자 일기 작성 성공"),
    SUCCESS_DELETE_EMOTION_DIARY(HttpStatus.OK, "DIARY2002", "감정 투자 일기가 삭제되었습니다."),

    // 인증
    AUTH_LOGIN_SUCCESS(HttpStatus.OK, "AUTH2000", "로그인 성공"),
    AUTH_SIGNUP_SUCCESS(HttpStatus.OK, "AUTH2001", "회원가입 성공"),
    AUTH_LOGOUT_SUCCESS(HttpStatus.OK, "AUTH2002", "로그아웃 성공"),
    AUTH_WITHDRAW_SUCCESS(HttpStatus.OK, "AUTH2003", "회원탈퇴 성공"),

    // User
    USER_PROFILE_SUCCESS(HttpStatus.OK, "USER2000", "프로필 조회 성공"),
    USER_PROFILE_UPDATE_SUCCESS(HttpStatus.OK, "USER2001", "프로필 수정 성공")
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
