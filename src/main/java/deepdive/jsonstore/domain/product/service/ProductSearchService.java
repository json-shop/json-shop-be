package deepdive.jsonstore.domain.product.service;

import deepdive.jsonstore.domain.product.dto.ProductResponse;
import deepdive.jsonstore.domain.product.entity.Product;
import deepdive.jsonstore.domain.product.entity.ProductDocument;
import deepdive.jsonstore.domain.product.entity.ProductStatus;
import deepdive.jsonstore.domain.product.exception.ProductException;
import deepdive.jsonstore.domain.product.repository.ProductEsRepository;
import deepdive.jsonstore.domain.product.repository.ProductRepository;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductSearchService {
    private final ProductEsRepository productEsRepository;
    private final ProductRepository productRepository;
    private final MeterRegistry meterRegistry;

//    public List<ProductDocument> searchByName(String name) {
//
//    }


    public ProductResponse getActiveProduct(String productId) {
        byte[] decodedId = Base64.getUrlDecoder().decode(productId);
//        ProductDocument productDocument = productEsRepository.findByIdAndStatusIsNot(decodedId, ProductStatus.DISCONTINUED)
//                .orElseThrow(ProductException.ProductNotFoundException::new);

        try {
            ProductDocument productDocument = productEsRepository.findById(decodedId)
                    .orElseThrow(ProductException.ProductNotFoundException::new);

            meterRegistry.counter("business.product.search.success").increment();

            return ProductResponse.toProductResponse(productDocument);

        } catch (ProductException.ProductNotFoundException e) {

            meterRegistry.counter("business.product.search.failure").increment();
            throw e;
        }
    }

    public ProductResponse getActiveProductJPA(String productId) {
        byte[] decodedId = Base64.getUrlDecoder().decode(productId);

        try {
            Product product = productRepository.findByUlidAndStatusIsNot(decodedId, ProductStatus.DISCONTINUED)
                    .orElseThrow(ProductException.ProductNotFoundException::new);

            meterRegistry.counter("business.product.search.success").increment();

            return ProductResponse.toProductResponse(product);

        } catch (ProductException.ProductNotFoundException e) {

            meterRegistry.counter("business.product.search.failure").increment();
            throw e;
        }
    }

}
