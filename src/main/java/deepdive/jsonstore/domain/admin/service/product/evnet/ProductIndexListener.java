package deepdive.jsonstore.domain.admin.service.product.evnet;

import deepdive.jsonstore.domain.product.entity.Product;
import deepdive.jsonstore.domain.product.entity.ProductDocument;
import deepdive.jsonstore.domain.product.repository.ProductEsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Base64;

@Component
@RequiredArgsConstructor
@Slf4j
@EnableAsync
public class ProductIndexListener {
    private final ProductEsRepository productEsRepository;


    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleProductCreated(ProductCreatedEvent event) {
        Product product = event.product();


        ProductDocument productDocument = ProductDocument.from(product);

        productEsRepository.save(productDocument);

        log.info("ES Product indexed: {}", Base64.getUrlEncoder().encodeToString(product.getUlid()));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleProductUpdated(ProductUpdatedEvent event) {
        Product product = event.product();
        ProductDocument productDocument = ProductDocument.from(product);

        productEsRepository.save(productDocument);
        log.info("ES Product indexed: {}", Base64.getUrlEncoder().encodeToString(product.getUlid()));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleProductDeleted(ProductDeletedEvent event) {
        Product product = event.product();
        ProductDocument productDocument = ProductDocument.from(product);

        productEsRepository.delete(productDocument);
        log.info("ES Product deleted: {}", Base64.getUrlEncoder().encodeToString(product.getUlid()));
    }
}
