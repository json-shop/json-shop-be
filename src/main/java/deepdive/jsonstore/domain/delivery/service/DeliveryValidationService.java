package deepdive.jsonstore.domain.delivery.service;

import deepdive.jsonstore.domain.delivery.exception.DeliveryException;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Service
public class DeliveryValidationService {

    private final RestTemplate restTemplate;

    private static final String BASE_URL = "https://www.juso.go.kr/addrlink/addrLinkApi.do";

    public DeliveryValidationService(RestTemplateBuilder builder) {
        this.restTemplate = builder.build(); // 테스트에서 주입 가능
    }

    @Value("${external.api.address.key:dummy-key}")
    private String ADDRESS_API_KEY;

    public boolean validateZipCode(String zipcode) {
        String url = UriComponentsBuilder.fromHttpUrl(BASE_URL)
                .queryParam("currentPage", 1)
                .queryParam("countPerPage", 1)
                .queryParam("keyword", zipcode)
                .queryParam("confmKey", ADDRESS_API_KEY)
                .queryParam("resultType", "json")
                .toUriString();

        String response = restTemplate.getForObject(url, String.class);

        JSONObject json = new JSONObject(response);

        JSONObject results = json.getJSONObject("results");
        JSONObject common = results.getJSONObject("common");
        String errorCode = common.getString("errorCode");
        String errorMessage = common.getString("errorMessage");

        //api key 또는 api 서버 문제
        if ("E0001".equals(errorCode) || "-999".equals(errorCode)) {
            log.warn("Address API returned error. code={}, message={}", errorCode, errorMessage);
            throw new DeliveryException.AddressAPIException();
        }

        JSONArray address = results.getJSONArray("juso");

        return address.length() > 0;

    }

}
