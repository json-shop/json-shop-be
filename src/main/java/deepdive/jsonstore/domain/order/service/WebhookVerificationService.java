package deepdive.jsonstore.domain.order.service;

import io.portone.sdk.server.errors.WebhookVerificationException;
import io.portone.sdk.server.webhook.Webhook;
import io.portone.sdk.server.webhook.WebhookTransaction;
import io.portone.sdk.server.webhook.WebhookVerifier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class WebhookVerificationService {

    private final WebhookVerifier webhookVerifier;

    public WebhookTransaction verify(String rawBody, String msgId, String signature, String timestamp) {

        Webhook test;

        try {

            test = webhookVerifier.verify(rawBody, msgId, signature, timestamp);
        } catch (WebhookVerificationException e) {
            throw new RuntimeException("???");
        }

        if (!(test instanceof WebhookTransaction)) {
            throw new RuntimeException("???");
        }

        return ((WebhookTransaction) test);

    }
}