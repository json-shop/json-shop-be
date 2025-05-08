package deepdive.jsonstore.domain.product.service;

import deepdive.jsonstore.domain.product.dto.ProductCache;
import deepdive.jsonstore.domain.product.dto.ProductResponse;
import deepdive.jsonstore.domain.product.dto.ProductSearchCondition;
import deepdive.jsonstore.domain.product.entity.Category;
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

	private static final String CACHE_KEY_BASE = "productPage:";
	private static final long TTL = 5; // 하드코딩했습니다.

	public ProductResponse getActiveProductDetail(String id) {
		Product product = productValidationService.findActiveProductById(id);
		meterRegistry.counter("business.product.viewed").increment();

		return ProductResponse.toProductResponse(product);
	}

	/** 조건에 따라 es조회 */
	public Page<ProductResponse> getProductList(ProductSearchCondition condition, Pageable pageable) {
		String name = condition.search();
		Category category = condition.category();
		boolean hasSearch = name != null;
		boolean hasCategory = category != null;
		boolean isFirstPage = pageable.getPageNumber() == 1;

		// 캐시 확인
		String cacheKey = CACHE_KEY_BASE + category + ":" + name + ":" + pageable.getSort();

		var cached = (ProductCache) redisTemplate.opsForValue().get(cacheKey);
		if (cached != null) { // && !cached.content().isEmpty() 아예 상품이 없는 경우
			log.info("from cache");
			return new PageImpl<>(cached.content(), pageable, cached.totalElements()); // 캐시를 바로 반환
		}

		// 쿼리
		Page<ProductDocument> productDocuments;
		if (hasSearch && !hasCategory) {
			productDocuments = productEsRepository.searchByName(name, pageable); // 키워드(이름)
		} else if (!hasSearch && hasCategory ) {
			productDocuments = productEsRepository.searchByCategory(category, pageable); // 카테고리
		} else if (hasSearch && hasCategory) {
			productDocuments = productEsRepository.searchByCategoryAndName(category, name, pageable); // 복합
		} else {
			productDocuments = productEsRepository.findAll(pageable); // 전체
		}

		// response 생성
		Page<ProductResponse> productResponses = productDocuments
				.map(ProductResponse::toProductResponse);

		// 첫 페이지라면 캐시
		if (isFirstPage) {
			ProductCache cache = ProductCache.builder()
					.content(productResponses.getContent())
					.totalElements(productResponses.getTotalElements())
					.build();
			redisTemplate.opsForValue().set(cacheKey, cache, Duration.ofMinutes(TTL));
		}

		return productResponses;
	}
}
