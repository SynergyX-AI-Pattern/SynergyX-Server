package com.synergyx.trading.service.userService;

import com.synergyx.trading.dto.user.ProfileUpdateRequestDTO;

public interface UserCommandService {

    /**
     * FCM 토큰을 업데이트합니다.
     *
     * @param userId   사용자 ID
     * @param fcmToken 새로운 FCM 토큰
     */
    void updateFcmToken(Long userId, String fcmToken);

    /**
     * 사용자 정보를 업데이트 합니다.
     *
     * @param userId
     * @param requestDTO 수정 요청 dto
     */
    void updateProfile(Long userId, ProfileUpdateRequestDTO requestDTO);
}
