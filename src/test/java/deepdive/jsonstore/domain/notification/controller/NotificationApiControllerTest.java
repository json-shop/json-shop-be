package deepdive.jsonstore.domain.notification.controller;

import com.google.gson.Gson;
import deepdive.jsonstore.common.advice.GlobalExceptionHandler;
import deepdive.jsonstore.domain.notification.dto.FcmTokenRequest;
import deepdive.jsonstore.domain.notification.dto.NotificationRequest;
import deepdive.jsonstore.domain.notification.service.NotificationService;
import deepdive.jsonstore.common.exception.CommonException;
import deepdive.jsonstore.common.exception.JsonStoreErrorCode;
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

import static org.mockito.Mockito.*;
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
                .setControllerAdvice(new GlobalExceptionHandler())  // 예외 핸들러 추가!
                .build();
    }

    @Nested
    @DisplayName("registerToken 테스트")
    class RegisterToken {

        @Test
        @DisplayName("성공")
        void success() throws Exception {
            // given
            FcmTokenRequest request = new FcmTokenRequest(1L, "validToken_123:ABC-def+ghi");

            // when
            ResultActions result = mockMvc.perform(
                    MockMvcRequestBuilders.post("/api/v1/fcm-tokens")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(gson.toJson(request))
            );

            // then
            result.andExpect(status().isOk())
                    .andExpect(content().string("FCM token registered successfully"));

            verify(notificationService, times(1))
                    .saveToken(request.getMemberId(), request.getToken());
        }

        @Test
        @DisplayName("실패 - memberId 없음")
        void fail_memberIdMissing() throws Exception {
            // given
            FcmTokenRequest request = new FcmTokenRequest(null, "validToken_123");

            // when
            ResultActions result = mockMvc.perform(
                    MockMvcRequestBuilders.post("/api/v1/fcm-tokens")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(gson.toJson(request))
            );

            // then
            result.andExpect(status().isBadRequest());

            verify(notificationService, never()).saveToken(any(), any());
        }

        @Test
        @DisplayName("실패 - memberId가 0 이하")
        void fail_memberIdNotPositive() throws Exception {
            // given
            FcmTokenRequest request = new FcmTokenRequest(0L, "validToken_123");

            // when
            ResultActions result = mockMvc.perform(
                    MockMvcRequestBuilders.post("/api/v1/fcm-tokens")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(gson.toJson(request))
            );

            // then
            result.andExpect(status().isBadRequest());

            verify(notificationService, never()).saveToken(any(), any());
        }

        @Test
        @DisplayName("실패 - token 없음")
        void fail_tokenMissing() throws Exception {
            // given
            FcmTokenRequest request = new FcmTokenRequest(1L, "");

            // when
            ResultActions result = mockMvc.perform(
                    MockMvcRequestBuilders.post("/api/v1/fcm-tokens")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(gson.toJson(request))
            );

            // then
            result.andExpect(status().isBadRequest());

            verify(notificationService, never()).saveToken(any(), any());
        }

        @Test
        @DisplayName("실패 - token 형식이 잘못됨")
        void fail_tokenInvalidPattern() throws Exception {
            // given
            FcmTokenRequest request = new FcmTokenRequest(1L, "invalid token@!#");

            // when
            ResultActions result = mockMvc.perform(
                    MockMvcRequestBuilders.post("/api/v1/fcm-tokens")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(gson.toJson(request))
            );

            // then
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
            // given
            NotificationRequest request = new NotificationRequest(1L, "제목", "내용");

            // when
            ResultActions result = mockMvc.perform(
                    MockMvcRequestBuilders.post("/api/v1/notifications")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(gson.toJson(request))
            );

            // then
            result.andExpect(status().isOk())
                    .andExpect(content().string("Notification sent successfully"));

            verify(notificationService, times(1))
                    .sendNotification(request.getMemberId(), request.getTitle(), request.getMessage());
        }

        @Test
        @DisplayName("실패 - memberId 없음")
        void fail_memberIdMissing() throws Exception {
            // given
            NotificationRequest request = new NotificationRequest(null, "제목", "내용");

            // when
            ResultActions result = mockMvc.perform(
                    MockMvcRequestBuilders.post("/api/v1/notifications")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(gson.toJson(request))
            );

            // then
            result.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").exists());

            verify(notificationService, never()).sendNotification(any(), any(), any());
        }

        @Test
        @DisplayName("실패 - memberId가 0 이하")
        void fail_memberIdNotPositive() throws Exception {
            // given
            NotificationRequest request = new NotificationRequest(0L, "제목", "내용");

            // when
            ResultActions result = mockMvc.perform(
                    MockMvcRequestBuilders.post("/api/v1/notifications")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(gson.toJson(request))
            );

            // then
            result.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").exists());

            verify(notificationService, never()).sendNotification(any(), any(), any());
        }

        @Test
        @DisplayName("실패 - title 없음")
        void fail_titleMissing() throws Exception {
            // given
            NotificationRequest request = new NotificationRequest(1L, null, "내용");

            // when
            ResultActions result = mockMvc.perform(
                    MockMvcRequestBuilders.post("/api/v1/notifications")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(gson.toJson(request))
            );

            // then
            result.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").exists());

            verify(notificationService, never()).sendNotification(any(), any(), any());
        }

        @Test
        @DisplayName("실패 - title 길이 초과")
        void fail_titleTooLong() throws Exception {
            // given
            String longTitle = "a".repeat(256);
            NotificationRequest request = new NotificationRequest(1L, longTitle, "내용");

            // when
            ResultActions result = mockMvc.perform(
                    MockMvcRequestBuilders.post("/api/v1/notifications")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(gson.toJson(request))
            );

            // then
            result.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").exists());

            verify(notificationService, never()).sendNotification(any(), any(), any());
        }

        @Test
        @DisplayName("실패 - message 없음")
        void fail_messageMissing() throws Exception {
            // given
            NotificationRequest request = new NotificationRequest(1L, "제목", null);

            // when
            ResultActions result = mockMvc.perform(
                    MockMvcRequestBuilders.post("/api/v1/notifications")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(gson.toJson(request))
            );

            // then
            result.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").exists());

            verify(notificationService, never()).sendNotification(any(), any(), any());
        }

        @Test
        @DisplayName("실패 - message 길이 초과")
        void fail_messageTooLong() throws Exception {
            // given
            String longMessage = "a".repeat(256);
            NotificationRequest request = new NotificationRequest(1L, "제목", longMessage);

            // when
            ResultActions result = mockMvc.perform(
                    MockMvcRequestBuilders.post("/api/v1/notifications")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(gson.toJson(request))
            );

            // then
            result.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").exists());

            verify(notificationService, never()).sendNotification(any(), any(), any());
        }
    }
}
