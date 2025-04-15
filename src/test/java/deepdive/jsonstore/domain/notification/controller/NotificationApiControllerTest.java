package deepdive.jsonstore.domain.notification.controller;

import com.google.gson.Gson;
import deepdive.jsonstore.common.advice.GlobalExceptionHandler;
import deepdive.jsonstore.domain.notification.dto.FcmTokenRequest;
import deepdive.jsonstore.domain.notification.dto.NotificationHistoryResponse;
import deepdive.jsonstore.domain.notification.dto.NotificationRequest;
import deepdive.jsonstore.domain.notification.entity.NotificationCategory;
import deepdive.jsonstore.domain.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class NotificationApiControllerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationApiController notificationApiController;

    private MockMvc mockMvc;
    private final Gson gson = new Gson();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(notificationApiController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .defaultRequest(MockMvcRequestBuilders.get("/")
                        .requestAttr("uid", UUID.randomUUID())) // 기본 request에 uid mock 설정
                .build();
    }

    @Nested
    @DisplayName("registerToken 테스트")
    class RegisterToken {

        @Test
        @DisplayName("성공")
        void success() throws Exception {
            UUID memberUid = UUID.randomUUID();
            String token = "validToken_123:ABC-def+ghi";

            FcmTokenRequest request = new FcmTokenRequest(token);

            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/fcm-tokens")
                            .param("memberUid", memberUid.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(gson.toJson(request)))
                    .andExpect(status().isOk())
                    .andExpect(content().string("FCM token registered successfully"));

            verify(notificationService).saveToken(eq(memberUid), eq(token));
        }

        @Test
        @DisplayName("실패 - token 없음")
        void fail_tokenMissing() throws Exception {
            FcmTokenRequest request = new FcmTokenRequest("");

            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/fcm-tokens")
                            .contentType(MediaType.APPLICATION_JSON)
                            .requestAttr("uid", UUID.randomUUID())
                            .content(gson.toJson(request)))
                    .andExpect(status().isBadRequest());

            verify(notificationService, never()).saveToken(any(), any());
        }

        @Test
        @DisplayName("실패 - token 형식이 잘못됨")
        void fail_tokenInvalidPattern() throws Exception {
            FcmTokenRequest request = new FcmTokenRequest("invalid token@!#");

            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/fcm-tokens")
                            .requestAttr("uid", UUID.randomUUID())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(gson.toJson(request)))
                    .andExpect(status().isBadRequest());

            verify(notificationService, never()).saveToken(any(), any());
        }
    }

    @Nested
    @DisplayName("sendNotification 테스트")
    class SendNotification {

        @Test
        @DisplayName("성공")
        void success() throws Exception {
            UUID memberUid = UUID.randomUUID();
            NotificationRequest request = new NotificationRequest("제목", "내용");

            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/notifications")
                            .param("memberUid", memberUid.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(gson.toJson(request)))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Notification sent successfully"));

            verify(notificationService).sendNotification(eq(memberUid), eq("제목"), eq("내용"), eq(NotificationCategory.SAVE));
        }

        @Test
        @DisplayName("실패 - title 없음")
        void fail_titleMissing() throws Exception {
            NotificationRequest request = new NotificationRequest(null, "내용");

            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/notifications")
                            .requestAttr("uid", UUID.randomUUID())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(gson.toJson(request)))
                    .andExpect(status().isBadRequest());

            verify(notificationService, never()).sendNotification(any(), any(), any(), any());
        }

        @Test
        @DisplayName("실패 - message 없음")
        void fail_messageMissing() throws Exception {
            NotificationRequest request = new NotificationRequest("제목", null);

            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/notifications")
                            .requestAttr("uid", UUID.randomUUID())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(gson.toJson(request)))
                    .andExpect(status().isBadRequest());

            verify(notificationService, never()).sendNotification(any(), any(), any(), any());
        }
    }

    @Nested
    @DisplayName("getNotificationHistory 테스트")
    class GetNotificationHistory {

        @Test
        @DisplayName("성공")
        void success() throws Exception {
            UUID memberUid = UUID.randomUUID();

            List<NotificationHistoryResponse> mockHistory = List.of(
                    new NotificationHistoryResponse(
                            1L, "제목", "내용", NotificationCategory.SAVE, memberUid, LocalDateTime.now())
            );

            when(notificationService.getNotificationHistory(memberUid)).thenReturn(mockHistory);

            mockMvc.perform(get("/api/v1/notifications")
                            .param("memberUid", memberUid.toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].title").value("제목"));

            verify(notificationService).getNotificationHistory(memberUid);
        }

        @Test
        @DisplayName("성공 - 알림 내역 없음")
        void success_empty() throws Exception {
            UUID memberUid = UUID.randomUUID();
            when(notificationService.getNotificationHistory(memberUid)).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/v1/notifications")
                            .param("memberUid", memberUid.toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));

            verify(notificationService).getNotificationHistory(memberUid);
        }
    }
}
