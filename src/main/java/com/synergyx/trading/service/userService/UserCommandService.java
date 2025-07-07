package com.synergyx.trading.service.userService;

public interface UserCommandService {

    /**
     * FCM 토큰을 업데이트합니다.
     * @param userId   사용자 ID
     * @param fcmToken 새로운 FCM 토큰
     */
    void updateFcmToken(Long userId, String fcmToken);
}
