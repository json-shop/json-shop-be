package deepdive.jsonstore.domain.product.service;

import deepdive.jsonstore.domain.product.dto.ProductListResponse;
import deepdive.jsonstore.domain.product.dto.ProductResponse;
import deepdive.jsonstore.domain.product.dto.ProductSearchCondition;
import deepdive.jsonstore.domain.product.entity.Product;
import deepdive.jsonstore.domain.product.entity.ProductDocument;
import deepdive.jsonstore.domain.product.repository.ProductEsRepository;
import deepdive.jsonstore.domain.product.repository.ProductQueryRepository;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceV2 {

	private final ProductValidationServiceV2 productValidationService;
	private final ProductEsRepository productEsRepository;
    private final MeterRegistry meterRegistry;
	public ProductResponse getActiveProductDetail(String id) {
		Product product = productValidationService.findActiveProductById(id);
		meterRegistry.counter("business.product.viewed").increment();

		return ProductResponse.toProductResponse(product);
	}

	public Page<ProductResponse> getProductList(ProductSearchCondition condition, Pageable pageable) {
		Page<ProductDocument> productDocuments = productEsRepository.searchByCategoryAndName(condition.category(),condition.search(), pageable);
		Page<ProductResponse> res = productDocuments.map(ProductResponse::toProductResponse);
		return res;
	}

}
