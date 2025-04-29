package deepdive.jsonstore.domain.product.service;

import deepdive.jsonstore.domain.product.dto.ProductResponse;
import deepdive.jsonstore.domain.product.entity.ProductDocument;
import deepdive.jsonstore.domain.product.exception.ProductException;
import deepdive.jsonstore.domain.product.repository.ProductEsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductSearchService {
    private final ProductEsRepository productEsRepository;


    public ProductResponse getActiveProduct(String productId) {
        byte[] decodedId = Base64.getUrlDecoder().decode(productId);
//        ProductDocument productDocument = productEsRepository.findByIdAndStatusIsNot(decodedId, ProductStatus.DISCONTINUED)
//                .orElseThrow(ProductException.ProductNotFoundException::new);
        ProductDocument productDocument = productEsRepository.findById(decodedId)
                .orElseThrow(ProductException.ProductNotFoundException::new);
        return ProductResponse.toProductResponse(productDocument);
    }
}
