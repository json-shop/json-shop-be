package deepdive.jsonstore.domain.order.service;

import deepdive.jsonstore.common.exception.CommonException;
import deepdive.jsonstore.domain.order.dto.CancelRequest;
import deepdive.jsonstore.domain.order.dto.ConfirmRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class PaymentService {

    @Value("${tosspayments.api-base}")
    private String apiBase;

    @Value("${tosspayments.api-secret}")
    private String secretKey;

    @Transactional
    public void cancelFullAmount(String paymentKey, String reason) {
        String url = apiBase + "/v1/payments/" + paymentKey +"/cancel";
        String auth = Base64.getEncoder().encodeToString((secretKey + ":").getBytes());

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Basic " + auth);

        var cancelRequest = CancelRequest.builder()
                .cancelReason(reason)
                .build();

        HttpEntity<CancelRequest> entity = new HttpEntity<>(cancelRequest, headers);

        restTemplate.postForObject(url, entity, String.class);
    }

    @Transactional
    public Map<String, Object> confirm(ConfirmRequest confirmRequest) {
        String url = apiBase + "/v1/payments/confirm";
        String auth = Base64.getEncoder().encodeToString((secretKey + ":").getBytes());

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Basic " + auth);

        HttpEntity<ConfirmRequest> entity = new HttpEntity<>(confirmRequest, headers);

        // Payment 객체를 응답합니다.
        // https://docs.tosspayments.com/reference
        try {
            Map<String, Object> paymentResponse = restTemplate.postForObject(url, entity, Map.class);
            return paymentResponse;
        }catch (HttpClientErrorException e) {
            log.info(e.getLocalizedMessage());
            throw new CommonException.InternalServerException();
        }


    }
}
