package com.synergyx.trading.service.fcmService;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.FirebaseMessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FCMService {

    public void sendPushNotification(String token, String title, String body) {
        Message message = Message.builder()
                .setToken(token)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .build();

        try {
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("[FCM] 푸시 알림 전송 성공: {}", response);
        } catch (FirebaseMessagingException e) {
            log.warn("[FCM] 푸시 알림 전송 실패: {}", e.getMessage(), e);
            throw new RuntimeException("[FCM] 전송 실패: " + e.getMessage(), e);
        }
    }
}
