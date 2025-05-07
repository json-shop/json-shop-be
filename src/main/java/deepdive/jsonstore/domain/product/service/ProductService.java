package deepdive.jsonstore.domain.product.service;

import java.util.UUID;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import deepdive.jsonstore.domain.product.dto.ProductListResponse;
import deepdive.jsonstore.domain.product.dto.ProductResponse;
import deepdive.jsonstore.domain.product.dto.ProductSearchCondition;
import deepdive.jsonstore.domain.product.entity.Product;
import deepdive.jsonstore.domain.product.repository.ProductQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

	private final ProductValidationService productValidationService;
	private final ProductQueryRepository productQueryRepository;
	private final MeterRegistry meterRegistry;

	public ProductResponse getActiveProductDetail(UUID id) {
		Product product = productValidationService.findActiveProductById(id);
		meterRegistry.counter("business.product.viewed").increment();
		return ProductResponse.toProductResponse(product);
	}

	public Page<ProductListResponse> getProductList(ProductSearchCondition condition, Pageable pageable) {
		return productQueryRepository.searchProductList(condition, pageable);
	}

}
