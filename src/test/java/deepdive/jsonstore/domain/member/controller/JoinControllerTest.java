package deepdive.jsonstore.domain.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import deepdive.jsonstore.common.config.RedisTestService;
import deepdive.jsonstore.common.exception.JoinException;
import deepdive.jsonstore.common.exception.JsonStoreErrorCode;
import deepdive.jsonstore.domain.member.dto.JoinRequest;
import deepdive.jsonstore.domain.member.service.JoinService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(JoinControllerTest.MockConfig.class)
class JoinControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JoinService joinService;

    @Mock
    private RedisTestService redisTestService;

    @TestConfiguration
    static class MockConfig {
        @Bean
        public JoinService joinService() {
            return Mockito.mock(JoinService.class);
        }

        @Bean
        public RedisTestService redisTestService() {
            return Mockito.mock(RedisTestService.class);
        }
    }
    @Test
    @DisplayName("회원가입 성공")
    void join_success() throws Exception {
        JoinRequest request = new JoinRequest(
                "test@example.com",
                "password123",
                "password123",
                "TestUser",
                "01012345678"
        );

        Mockito.doNothing().when(joinService).joinProcess(Mockito.any(JoinRequest.class));

        mockMvc.perform(post("/api/v1/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("회원가입이 완료되었습니다."));

    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    void join_fail_duplicate_email() throws Exception {
        JoinRequest request = new JoinRequest(
                "test@example.com",
                "password123",
                "password123",
                "TestUser",
                "01012345678"
        );

        doThrow(new JoinException.DuplicateEmailException(JsonStoreErrorCode.DUPLICATE_EMAIL))
                .when(joinService).joinProcess(Mockito.any(JoinRequest.class));

        mockMvc.perform(post("/api/v1/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("DUPLICATE_EMAIL"))
                .andExpect(jsonPath("$.message").value("이미 존재하는 이메일입니다."));
    }

    @Test
    @DisplayName("회원가입 실패 - 비밀번호 불일치")
    void join_fail_password_mismatch() throws Exception {
        JoinRequest request = new JoinRequest(
                "test@example.com",
                "password123",
                "differentPassword",
                "TestUser",
                "01012345678"
        );

        doThrow(new JoinException.PasswordMismatchException(JsonStoreErrorCode.PASSWORD_MISMATCH))
                .when(joinService).joinProcess(Mockito.any(JoinRequest.class));

        mockMvc.perform(post("/api/v1/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PASSWORD_MISMATCH"))
                .andExpect(jsonPath("$.message").value("비밀번호가 일치하지 않습니다."));
    }

}
