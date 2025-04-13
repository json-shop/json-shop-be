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
        mockMvc = MockMvcBuilders.standaloneSetup(notificationApiController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Nested
    @DisplayName("registerToken 테스트")
    class RegisterToken {

        @Test
        @DisplayName("성공")
        void success() throws Exception {
            UUID memberUid = UUID.randomUUID();
            FcmTokenRequest request = new FcmTokenRequest(memberUid, "validToken_123:ABC-def+ghi");

            ResultActions result = mockMvc.perform(
                    MockMvcRequestBuilders.post("/api/v1/fcm-tokens")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(gson.toJson(request))
            );

            result.andExpect(status().isOk())
                    .andExpect(content().string("FCM token registered successfully"));

            verify(notificationService).saveToken(eq(request.getMemberUid()), eq(request.getToken()));
        }

        @Test
        @DisplayName("실패 - memberUid 없음")
        void fail_memberUidMissing() throws Exception {
            FcmTokenRequest request = new FcmTokenRequest(null, "validToken_123");

            ResultActions result = mockMvc.perform(
                    MockMvcRequestBuilders.post("/api/v1/fcm-tokens")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(gson.toJson(request))
            );

            result.andExpect(status().isBadRequest());
            verify(notificationService, never()).saveToken(any(), any());
        }

        @Test
        @DisplayName("실패 - token 없음")
        void fail_tokenMissing() throws Exception {
            FcmTokenRequest request = new FcmTokenRequest(UUID.randomUUID(), "");

            ResultActions result = mockMvc.perform(
                    MockMvcRequestBuilders.post("/api/v1/fcm-tokens")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(gson.toJson(request))
            );

            result.andExpect(status().isBadRequest());
            verify(notificationService, never()).saveToken(any(), any());
        }

        @Test
        @DisplayName("실패 - token 형식이 잘못됨")
        void fail_tokenInvalidPattern() throws Exception {
            FcmTokenRequest request = new FcmTokenRequest(UUID.randomUUID(), "invalid token@!#");

            ResultActions result = mockMvc.perform(
                    MockMvcRequestBuilders.post("/api/v1/fcm-tokens")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(gson.toJson(request))
            );

            result.andExpect(status().isBadRequest());
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
            NotificationRequest request = new NotificationRequest(memberUid, "제목", "내용");

            ResultActions result = mockMvc.perform(
                    MockMvcRequestBuilders.post("/api/v1/notifications")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(gson.toJson(request))
            );

            result.andExpect(status().isOk())
                    .andExpect(content().string("Notification sent successfully"));

            verify(notificationService).sendNotification(eq(request.getMemberUid()), eq(request.getTitle()), eq(request.getMessage()), any());
        }

        @Test
        @DisplayName("실패 - memberUid 없음")
        void fail_memberUidMissing() throws Exception {
            NotificationRequest request = new NotificationRequest(null, "제목", "내용");

            ResultActions result = mockMvc.perform(
                    MockMvcRequestBuilders.post("/api/v1/notifications")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(gson.toJson(request))
            );

            result.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").exists());

            verify(notificationService, never()).sendNotification(any(), any(), any(), any());
        }

        @Test
        @DisplayName("실패 - title 없음")
        void fail_titleMissing() throws Exception {
            NotificationRequest request = new NotificationRequest(UUID.randomUUID(), null, "내용");

            ResultActions result = mockMvc.perform(
                    MockMvcRequestBuilders.post("/api/v1/notifications")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(gson.toJson(request))
            );

            result.andExpect(status().isBadRequest());
            verify(notificationService, never()).sendNotification(any(), any(), any(), any());
        }

        @Test
        @DisplayName("실패 - title 길이 초과")
        void fail_titleTooLong() throws Exception {
            String longTitle = "a".repeat(256);
            NotificationRequest request = new NotificationRequest(UUID.randomUUID(), longTitle, "내용");

            ResultActions result = mockMvc.perform(
                    MockMvcRequestBuilders.post("/api/v1/notifications")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(gson.toJson(request))
            );

            result.andExpect(status().isBadRequest());
            verify(notificationService, never()).sendNotification(any(), any(), any(), any());
        }

        @Test
        @DisplayName("실패 - message 없음")
        void fail_messageMissing() throws Exception {
            NotificationRequest request = new NotificationRequest(UUID.randomUUID(), "제목", null);

            ResultActions result = mockMvc.perform(
                    MockMvcRequestBuilders.post("/api/v1/notifications")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(gson.toJson(request))
            );

            result.andExpect(status().isBadRequest());
            verify(notificationService, never()).sendNotification(any(), any(), any(), any());
        }

        @Test
        @DisplayName("실패 - message 길이 초과")
        void fail_messageTooLong() throws Exception {
            String longMessage = "a".repeat(256);
            NotificationRequest request = new NotificationRequest(UUID.randomUUID(), "제목", longMessage);

            ResultActions result = mockMvc.perform(
                    MockMvcRequestBuilders.post("/api/v1/notifications")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(gson.toJson(request))
            );

            result.andExpect(status().isBadRequest());
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
                            1L,
                            "제목",
                            "내용",
                            NotificationCategory.SAVE,
                            memberUid,
                            LocalDateTime.now()
                    )
            );

            when(notificationService.getNotificationHistory(memberUid)).thenReturn(mockHistory);

            mockMvc.perform(get("/api/v1/notifications")
                            .param("memberUid", memberUid.toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(1));

            verify(notificationService).getNotificationHistory(memberUid);
        }

        @Test
        @DisplayName("실패 - memberUid 없음")
        void fail_memberUidMissing() throws Exception {
            mockMvc.perform(get("/api/v1/notifications")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(notificationService, never()).getNotificationHistory(any());
        }

        @Test
        @DisplayName("성공 - 알림 내역 없음")
        void success_emptyNotificationHistory() throws Exception {
            UUID memberUid = UUID.randomUUID();
            when(notificationService.getNotificationHistory(memberUid)).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/v1/notifications")
                            .param("memberUid", memberUid.toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));

            verify(notificationService).getNotificationHistory(memberUid);
        }
    }
}
