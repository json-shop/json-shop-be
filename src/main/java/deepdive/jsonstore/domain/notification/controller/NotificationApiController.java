package deepdive.jsonstore.domain.notification.controller;

import deepdive.jsonstore.domain.notification.dto.FcmTokenRequest;
import deepdive.jsonstore.domain.notification.dto.NotificationRequest;
import deepdive.jsonstore.domain.notification.entity.Notification;
import deepdive.jsonstore.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class NotificationApiController {

    private final NotificationService notificationService;

    @PostMapping("/fcm-tokens")
    public ResponseEntity<String> registerToken(@RequestBody FcmTokenRequest request) {
        notificationService.saveToken(request.getMemberId(), request.getToken());
        return ResponseEntity.ok("FCM token registered successfully");
    }
}