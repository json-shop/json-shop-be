package deepdive.jsonstore.domain.delivery.service;

import deepdive.jsonstore.domain.delivery.exception.DeliveryException;
import deepdive.jsonstore.domain.delivery.repository.DeliveryRepository;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class DeliveryAddressValidationService {
    private final RestTemplate restTemplate;

    private static final String BASE_URL = "https://www.juso.go.kr/addrlink/addrLinkApi.do";
    private final DeliveryRepository deliveryRepository;

    public DeliveryAddressValidationService(RestTemplateBuilder builder, DeliveryRepository deliveryRepository) {
        this.restTemplate = builder.build(); // 테스트에서 주입 가능
        this.deliveryRepository = deliveryRepository;
    }

    @Value("${external.api.address.key:dummy-key}")
    private String ADDRESS_API_KEY;

    public void validateZipCode(String zipcode) {
        String url = BASE_URL + "?currentPage=1&countPerPage=1"
                + "&keyword=" + URLEncoder.encode(zipcode, StandardCharsets.UTF_8)
                + "&confmKey=" + ADDRESS_API_KEY
                + "&resultType=json";

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

        if (address.length() <= 0){
            throw new DeliveryException.AddressNotFoundException();
        }

    }
}
