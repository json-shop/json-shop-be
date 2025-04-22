package deepdive.jsonstore.domain.product.service;

import deepdive.jsonstore.domain.product.dto.ProductResponse;
import deepdive.jsonstore.domain.product.entity.Product;
import deepdive.jsonstore.domain.product.repository.ProductQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceV2 {

	private final ProductValidationService productValidationService;
	private final ProductQueryRepository productQueryRepository;

	public ProductResponse getActiveProductDetail(UUID id) {
		Product product = productValidationService.findActiveProductById(id);
		return ProductResponse.toProductResponse(product);
	}
}
