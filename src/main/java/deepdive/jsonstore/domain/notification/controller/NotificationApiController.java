package deepdive.jsonstore.domain.notification.controller;

import deepdive.jsonstore.domain.notification.dto.FcmTokenRequest;
import deepdive.jsonstore.domain.notification.dto.NotificationHistoryRequest;
import deepdive.jsonstore.domain.notification.dto.NotificationRequest;
import deepdive.jsonstore.domain.notification.entity.Notification;
import deepdive.jsonstore.domain.notification.service.NotificationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class NotificationApiController {

    private final NotificationService notificationService;

    // FCM token 저장
    @PostMapping("/fcm-tokens")
    public ResponseEntity<String> registerToken(@Valid @RequestBody FcmTokenRequest request) {
        notificationService.saveToken(request.getMemberId(), request.getToken());
        return ResponseEntity.ok("FCM token registered successfully");
    }

    // 사용자 알림 전송
    @PostMapping("/notifications")
    public ResponseEntity<String> sendNotification(@Valid @RequestBody NotificationRequest request) {
        notificationService.sendNotification(
                request.getMemberId(),
                request.getTitle(),
                request.getMessage()
        );
        return ResponseEntity.ok("Notification sent successfully");
    }

    // 특정 멤버 알림 내역 조회
    @GetMapping("/notifications")
    public ResponseEntity<List<Notification>> getNotificationHistory(@Valid NotificationHistoryRequest request) {
        List<Notification> history = notificationService.getNotificationHistory(request.getMemberId());
        return ResponseEntity.ok(history);
    }
}