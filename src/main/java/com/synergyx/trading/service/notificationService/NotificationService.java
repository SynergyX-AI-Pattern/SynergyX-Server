package com.synergyx.trading.service.notificationService;

import com.synergyx.trading.dto.notification.NotificationRequestDTO;
import com.synergyx.trading.dto.notification.NotificationResponseDTO;

public interface NotificationService {
    NotificationResponseDTO sendAndSaveNotification(Long userId, NotificationRequestDTO dto);
}
