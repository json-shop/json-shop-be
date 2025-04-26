package deepdive.jsonstore.domain.product.controller;

import deepdive.jsonstore.domain.product.dto.ProductResponse;
import deepdive.jsonstore.domain.product.entity.ProductDocument;
import deepdive.jsonstore.domain.product.repository.ProductEsRepository;
import deepdive.jsonstore.domain.product.repository.ProductRepository;
import deepdive.jsonstore.domain.product.service.ProductSearchService;
import deepdive.jsonstore.domain.product.service.ProductServiceV2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;
import java.util.List;
import java.util.UUID;


@RequestMapping("/api/v2/products")
@RequiredArgsConstructor
@RestController
@Slf4j
public class ProductControllerV2 {
	private final ProductServiceV2 productService;
	private final ProductSearchService productSearchService;

	@GetMapping("/{productId}")
	public ResponseEntity<ProductResponse> getActiveProduct(@PathVariable String productId) {
		log.info("productId: {}", productId);
		ProductResponse res = productService.getActiveProductDetail(productId);
		return ResponseEntity.ok(res);
	}

	@GetMapping("/search/{productId}")
	public ResponseEntity<ProductResponse> getProduct(@PathVariable String productId) {
		log.info("productId: {}", productId);
		ProductResponse res = productSearchService.getActiveProduct(productId);
		return ResponseEntity.ok(res);
	}
}
