package deepdive.jsonstore.domain.admin.service.product.evnet;

import deepdive.jsonstore.domain.product.entity.Product;
import deepdive.jsonstore.domain.product.entity.ProductDocument;
import deepdive.jsonstore.domain.product.repository.ProductEsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductIndexListener {
    private final ProductEsRepository productEsRepository;


    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductCreated(ProductCreatedEvent event) {
        Product product = event.product();


        ProductDocument productDocument = ProductDocument.from(product);

        productEsRepository.save(productDocument);
        log.info("ES Product indexed: {}", productDocument.getId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductUpdated(ProductUpdatedEvent event) {
        Product product = event.product();
        ProductDocument productDocument = ProductDocument.from(product);

        productEsRepository.save(productDocument);
        log.info("ES Product indexed: {}", productDocument.getId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductDeleted(ProductDeletedEvent event) {
        Product product = event.product();
        ProductDocument productDocument = ProductDocument.from(product);

        productEsRepository.delete(productDocument);
        log.info("ES Product deleted: {}", productDocument.getId());
    }


}
