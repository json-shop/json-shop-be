package deepdive.jsonstore.domain.order.service;

import deepdive.jsonstore.common.exception.CommonException;
import deepdive.jsonstore.domain.order.dto.CancelRequest;
import deepdive.jsonstore.domain.order.dto.ConfirmRequest;
import io.micrometer.core.instrument.MeterRegistry;
import deepdive.jsonstore.domain.order.dto.ConfirmRequestV2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

///**
// * 400 - Bad Request	요청을 처리할 수 없습니다. 필수 파라미터를 보내지 않았거나, 파라미터 포맷이 잘못되었을 때 돌아오는 응답입니다. 요청 파라미터를 확인해주세요.
// * 403 - Forbidden	시크릿 키 없이 요청했거나 사용한 시크릿 키가 잘못되었습니다. 개발자센터에서 내 상점의 키값을 다시 한번 확인하고, 시크릿 키 문서를 참고하세요.
// * 404 - Not Found	요청한 리소스가 존재하지 않습니다. 요청한 API 주소를 다시 한번 확인해보세요.
// * 429 - Too Many Requests	비정상적으로 많은 요청을 보냈습니다. 잠시 후 다시 시도해주세요.
// * 500 - Server Error	토스페이먼츠 서버에서 에러가 발생했습니다.
// */
@Slf4j
@EnableAsync
@RequiredArgsConstructor
@Service
public class PaymentService {

    @Value("${tosspayments.api-base}")
    private String apiBase;

    @Value("${tosspayments.api-secret}")
    private String secretKey;

    private final MeterRegistry meterRegistry;

    @Transactional
    public void cancelFullAmount(String paymentKey, String reason) {
        String url = apiBase + "/v1/payments/" + paymentKey + "/cancel";
        String auth = Base64.getEncoder().encodeToString((secretKey + ":").getBytes());

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Basic " + auth);

        var cancelRequest = CancelRequest.builder()
                .cancelReason(reason)
                .build();

        HttpEntity<CancelRequest> entity = new HttpEntity<>(cancelRequest, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new CommonException.InternalServerException();
            }
        } catch (HttpClientErrorException e) {
            log.info(e.getLocalizedMessage());
            throw new CommonException.InternalServerException();
        }
    }

    @Transactional
    public Map<String, Object> confirm(ConfirmRequest confirmRequest) {
        log.info("confirmRequest : {}", confirmRequest);

        String url = apiBase + "/v1/payments/confirm";
        String auth = Base64.getEncoder().encodeToString((secretKey + ":").getBytes());

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Basic " + auth);

        HttpEntity<ConfirmRequest> entity = new HttpEntity<>(confirmRequest, headers);
        try {
            ResponseEntity<Map> response =
                    restTemplate.postForEntity(url, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                // ⭐ 결제 성공 메트릭 추가
                meterRegistry.counter("business.payment.success").increment();

                return response.getBody();
            } else {
                // ⭐ 결제 실패 메트릭 추가
                meterRegistry.counter("business.payment.failure").increment();

                throw new CommonException.InternalServerException();
            }


        } catch (HttpClientErrorException e) {
            log.info("LOG : {}", e.getLocalizedMessage());
            throw new CommonException.InternalServerException();
        }
    }

    @Async
    @Transactional
    public CompletableFuture<Map<String, Object>> ayncConfirm(ConfirmRequest confirmRequest) {
        String url = apiBase + "/v1/payments/confirm";
        String auth = Base64.getEncoder().encodeToString((secretKey + ":").getBytes());

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Basic " + auth);

        HttpEntity<ConfirmRequest> entity = new HttpEntity<>(confirmRequest, headers);
        try {
            ResponseEntity<Map> response =
                    restTemplate.postForEntity(url, entity, Map.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new CommonException.InternalServerException();
            }

            return CompletableFuture.completedFuture(response.getBody());
        } catch (HttpClientErrorException e) {
            log.info(e.getLocalizedMessage());
            throw new CommonException.InternalServerException();
        }
    }

    @Async
    public void confirmTest(ConfirmRequestV2 confirmRequest) {
        var restTamplate = new RestTemplate();

        restTamplate.postForEntity(
                "http://localhost:8080/webhook",
                confirmRequest.orderId(),
                String.class
        );
    }
}
