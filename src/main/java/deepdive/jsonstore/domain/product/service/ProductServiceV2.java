package deepdive.jsonstore.domain.product.service;

import deepdive.jsonstore.domain.product.dto.ProductCache;
import deepdive.jsonstore.domain.product.dto.ProductResponse;
import deepdive.jsonstore.domain.product.dto.ProductSearchCondition;
import deepdive.jsonstore.domain.product.entity.Product;
import deepdive.jsonstore.domain.product.entity.ProductDocument;
import deepdive.jsonstore.domain.product.repository.ProductEsRepository;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceV2 {

	private final ProductValidationServiceV2 productValidationService;
	private final ProductEsRepository productEsRepository;
    private final MeterRegistry meterRegistry;
	private final RedisTemplate<String, Object> redisTemplate;
	private static final String CACHE_KEY = "productPage:";
	private static final long TTL = 5;

	public ProductResponse getActiveProductDetail(String id) {
		Product product = productValidationService.findActiveProductById(id);
		meterRegistry.counter("business.product.viewed").increment();

		return ProductResponse.toProductResponse(product);
	}

	public Page<ProductResponse> getProductList(ProductSearchCondition condition, Pageable pageable) {
		if (condition.isEmpty()
				&& pageable.getSort().isUnsorted()
				&& pageable.getPageNumber() == 0) {


			ProductCache cached = (ProductCache) redisTemplate.opsForValue().get(CACHE_KEY);

			if (cached != null) { // && !cached.content().isEmpty() 아예 상품이 없는 경우
				log.info("from cache");
				return new PageImpl<>(cached.content(), pageable, cached.totalElements());
			}

//			log.info("from All");
			Page<ProductResponse> result = productEsRepository.findAll(pageable)
					.map(ProductResponse::toProductResponse);

			ProductCache cache = ProductCache.builder()
					.content(result.getContent())
					.totalElements(result.getTotalElements())
					.build();

			redisTemplate.opsForValue().set(CACHE_KEY, cache, Duration.ofMinutes(TTL));
			return result;
		}

		Page<ProductDocument> productDocuments = productEsRepository.searchByCategoryAndName(condition.category(), condition.search(), pageable);
		return productDocuments.map(ProductResponse::toProductResponse);
	}

}
