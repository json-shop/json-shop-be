package deepdive.jsonstore.common.config;

import io.portone.sdk.server.PortOneClient;
import io.portone.sdk.server.payment.PaymentClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class PortOneConfig {

    @Value("${portone.api-key}") // v1용
    private String API_KEY;

    @Value("${portone.api-base}")
    private String API_BASE;

    @Value("${portone.store-id}")
    private String STORE_ID;

    @Value("${portone.api-secret}")
    private String API_SECRET;

    @Bean
    public PaymentClient paymentClient(PortOneClient portOneClient) {
//        return new PaymentClient(API_KEY, API_BASE, STORE_ID);
        return portOneClient.getPayment();
    }

    @Bean
    public PortOneClient portOneClient() {
        return new PortOneClient(API_SECRET, API_BASE, STORE_ID);
    }

}
