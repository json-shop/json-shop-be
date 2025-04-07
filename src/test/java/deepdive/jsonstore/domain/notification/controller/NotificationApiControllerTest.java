package deepdive.jsonstore.domain.notification.controller;

import com.google.gson.Gson;
import deepdive.jsonstore.common.advice.GlobalExceptionHandler;
import deepdive.jsonstore.domain.notification.dto.FcmTokenRequest;
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
            FcmTokenRequest request = new FcmTokenRequest(1L, "valid-token");

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
            FcmTokenRequest request = new FcmTokenRequest(null, "token");

            doThrow(new CommonException(JsonStoreErrorCode.UNAUTHORIZED))
                    .when(notificationService).saveToken(null, "token");

            // when
            ResultActions result = mockMvc.perform(
                    MockMvcRequestBuilders.post("/api/v1/fcm-tokens")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(gson.toJson(request))
            );

            // then
            result.andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("실패 - token 없음")
        void fail_tokenMissing() throws Exception {
            // given
            FcmTokenRequest request = new FcmTokenRequest(1L, "");

            doThrow(new CommonException.InvalidInputException())
                    .when(notificationService).saveToken(1L, "");

            // when
            ResultActions result = mockMvc.perform(
                    MockMvcRequestBuilders.post("/api/v1/fcm-tokens")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(gson.toJson(request))
            );

            // then
            result.andExpect(status().isBadRequest());
        }
    }
}
