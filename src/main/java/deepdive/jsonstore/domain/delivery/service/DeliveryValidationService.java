package deepdive.jsonstore.domain.delivery.service;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Service
public class DeliveryValidationService {

    private final RestTemplate restTemplate = new RestTemplate();


    @Value("${external.api.address.key:dummy-key}")
    private String ADDRESS_API_KEY;

    public boolean validateZipcode(String zipcode) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl("https://www.juso.go.kr/addrlink/addrLinkApi.do")
                    .queryParam("currentPage", 1)
                    .queryParam("countPerPage", 1)
                    .queryParam("keyword", zipcode)
                    .queryParam("confmKey", ADDRESS_API_KEY)
                    .queryParam("resultType", "json")
                    .toUriString();

            String response = restTemplate.getForObject(url, String.class);

            JSONObject json = new JSONObject(response);
            JSONArray address = json.getJSONObject("results").getJSONArray("juso");

            return address.length() > 0;

        }catch (Exception e){ //나중에 수정
            return false;
        }

    }

}
