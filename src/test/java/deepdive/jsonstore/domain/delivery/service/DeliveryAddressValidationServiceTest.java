//package deepdive.jsonstore.domain.delivery.service;
//
//import com.google.firebase.messaging.FirebaseMessaging;
//import deepdive.jsonstore.common.config.FirebaseConfig;
//import deepdive.jsonstore.common.config.RedisTestService;
//import deepdive.jsonstore.domain.delivery.exception.DeliveryException;
//import jakarta.transaction.Transactional;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mock;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.context.TestConfiguration;
//import org.springframework.boot.web.client.RestTemplateBuilder;
//import org.springframework.context.annotation.Bean;
//import org.springframework.test.annotation.Rollback;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.web.client.RestTemplate;
//import software.amazon.awssdk.services.s3.S3Client;
//
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//
//@SpringBootTest
//@Transactional
//@Rollback
//@DisplayName("deliveryAddressValidationService 테스트")
//class DeliveryAddressValidationServiceTest {
//
//    @MockitoBean
//    private FirebaseConfig firebaseConfig;
//
//    @MockitoBean
//    private RedisTestService redisTestService;
//
//    @MockitoBean
//    private S3Client s3Client;
//
//    @MockitoBean
//    private FirebaseMessaging firebaseMessaging;
//
//    @MockitoBean
//    private RestTemplate restTemplate;
//
//    @Mock
//    private RestTemplateBuilder restTemplateBuilder;
//
//    @Autowired
//    private DeliveryAddressValidationService deliveryAddressValidationService;
//
//    @TestConfiguration
//    static class TestConfig {
//        @Bean
//        public RestTemplate restTemplate() {
//            return mock(RestTemplate.class);
//        }
//        @Bean
//        public RestTemplateBuilder restTemplateBuilder(RestTemplate restTemplate) {
//            // 테스트용 RestTemplate 주입
//            return new RestTemplateBuilder() {
//                @Override
//                public RestTemplate build() {
//                    return restTemplate;
//                }
//            };
//        }
//    }
//
//    void setUp() {
//        when(restTemplateBuilder.build()).thenReturn(restTemplate);
//    }
//
//    @Nested
//    @DisplayName("우편번호 검증 테스트")
//    class ValidateZipCodeTest {
//        @Test
//        @DisplayName("성공")
//        void success(){
//            //given
//            String zipCode = "12345";
//            String mockResponse = """
//        {
//            "results": {
//                "common": {
//                    "errorCode": "0",
//                    "errorMessage": "정상"
//                },
//                "juso": [
//                    {
//                        "zipNo": "12345"
//                    }
//                ]
//            }
//        }
//        """;
//
//            when(restTemplate.getForObject(anyString(),eq(String.class))).thenReturn(mockResponse);
//
//            //when & then
//            assertDoesNotThrow(() -> deliveryAddressValidationService.validateZipCode(zipCode));
//
//        }
//
//        @Test
//        @DisplayName("실패- api 오류")
//        void failure_ApiNotWorking(){
//            // given
//            String zipCode = "12345";
//            String errorResponse = """
//        {
//            "results": {
//                "common": {
//                    "errorCode": "E0001",
//                    "errorMessage": "인증키 오류"
//                },
//                "juso": []
//            }
//        }
//        """;
//
//            when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(errorResponse);
//
//            // then
//            assertThatThrownBy(() -> deliveryAddressValidationService.validateZipCode(zipCode))
//                    .isInstanceOf(DeliveryException.AddressAPIException.class);
//        }
//        @Test
//        @DisplayName("실패- 존재하지 않는 주소")
//        void failure_invalidZipCode(){
//            // given
//            String zipCode = "12345";
//            String errorResponse = """
//        {
//            "results": {
//                "common": {
//                    "errorCode": "0",
//                    "errorMessage": "정상"
//                },
//                "juso": []
//            }
//        }
//        """;
//            when(restTemplate.getForObject(anyString(),eq(String.class))).thenReturn(errorResponse);
//
//            //when & then
//            assertThrows(DeliveryException.AddressNotFoundException.class,
//                    () -> deliveryAddressValidationService.validateZipCode("12345"));
//
//        }
//    }
//
//}