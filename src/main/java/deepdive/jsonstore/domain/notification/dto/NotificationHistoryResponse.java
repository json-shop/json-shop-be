package deepdive.jsonstore.domain.notification.dto;

import deepdive.jsonstore.domain.notification.entity.NotificationCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class NotificationHistoryResponse {
    private Long id;
    private String title;
    private String body;
    private NotificationCategory category;
    private Long memberId;
    private LocalDateTime createdAt;
}
