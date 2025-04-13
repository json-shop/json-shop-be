package deepdive.jsonstore.domain.notification.dto;

import deepdive.jsonstore.domain.notification.entity.NotificationCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class NotificationHistoryResponse {
    private Long id;
    private String title;
    private String body;
    private NotificationCategory category;
    private UUID memberUid;
    private LocalDateTime createdAt;
}
