package deepdive.jsonstore.domain.notification.controller;

import deepdive.jsonstore.domain.notification.dto.FcmTokenRequest;
import deepdive.jsonstore.domain.notification.dto.NotificationHistoryResponse;
import deepdive.jsonstore.domain.notification.dto.NotificationRequest;
import deepdive.jsonstore.domain.notification.entity.NotificationCategory;
import deepdive.jsonstore.domain.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class NotificationApiController {

    private final NotificationService notificationService;

    // FCM token 저장
    @PostMapping("/fcm-tokens")
    public ResponseEntity<String> registerToken(
            @AuthenticationPrincipal(expression = "uid") UUID memberUid,
            @Valid @RequestBody FcmTokenRequest request
    ) {
        notificationService.saveToken(memberUid, request.getToken());
        return ResponseEntity.ok("FCM token registered successfully");
    }

    // 사용자 알림 전송
    @PostMapping("/notifications")
    public ResponseEntity<String> sendNotification(
            @AuthenticationPrincipal(expression = "uid") UUID memberUid,
            @Valid @RequestBody NotificationRequest request
    ) {
        notificationService.sendNotification(
                memberUid,
                request.getTitle(),
                request.getMessage(),
                NotificationCategory.SAVE
        );
        return ResponseEntity.ok("Notification sent successfully");
    }

    // 특정 멤버 알림 내역 조회
    @GetMapping("/notifications")
    public ResponseEntity<List<NotificationHistoryResponse>> getNotificationHistory(
            @AuthenticationPrincipal(expression = "uid") UUID memberUid
    ) {
        List<NotificationHistoryResponse> history = notificationService.getNotificationHistory(memberUid);
        return ResponseEntity.ok(history);
    }
}