package com.synergyx.trading.service.notificationService;

import com.synergyx.trading.apiPayload.code.status.ErrorStatus;
import com.synergyx.trading.apiPayload.exception.GeneralException;
import com.synergyx.trading.dto.notification.NotificationRequestDTO;
import com.synergyx.trading.dto.notification.NotificationResponseDTO;
import com.synergyx.trading.enums.NotificationType;
import com.synergyx.trading.model.Notification;
import com.synergyx.trading.model.User;
import com.synergyx.trading.repository.NotificationRepository;
import com.synergyx.trading.repository.UserRepository;
import com.synergyx.trading.service.fcmService.FCMService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final FCMService fcmService;

    @Override
    @Transactional
    public NotificationResponseDTO sendAndSaveNotification(Long userId, NotificationRequestDTO dto) {

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

        if (user.getFcmToken() == null || user.getFcmToken().isBlank()) {
            throw new GeneralException(ErrorStatus.FCM_TOKEN_NOT_FOUND);
        }

        NotificationType type;
        try {
            type = NotificationType.fromCode(dto.getType());
        } catch (IllegalArgumentException e) {
            throw new GeneralException(ErrorStatus.INVALID_NOTIFICATION_TYPE);
        }

        Notification notification = Notification.builder()
                .user(user)
                .title(dto.getTitle())
                .message(dto.getMessage())
                .isRead(false)
                .isValid(true)
                .type(type)
                .build();

        Notification saved = notificationRepository.saveAndFlush(notification);

        try {
            fcmService.sendPushNotification(user.getFcmToken(), dto.getTitle(), dto.getMessage());
        } catch (RuntimeException  e) {
            throw new GeneralException(ErrorStatus.INVALID_FCM_TOKEN);
        }

        return NotificationResponseDTO.builder()
                .id(saved.getId())
                .title(saved.getTitle())
                .message(saved.getMessage())
                .isRead(saved.getIsRead())
                .isValid(saved.getIsValid())
                .createdAt(saved.getCreatedAt())
                .type(saved.getType().name())
                .build();
    }

}
