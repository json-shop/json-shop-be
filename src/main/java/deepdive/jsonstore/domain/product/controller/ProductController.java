package deepdive.jsonstore.domain.product.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import deepdive.jsonstore.domain.product.dto.ProductListResponse;
import deepdive.jsonstore.domain.product.dto.ProductResponse;
import deepdive.jsonstore.domain.product.dto.ProductSearchCondition;
import deepdive.jsonstore.domain.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@RestController
@Slf4j
public class ProductController {
	private final ProductService productService;

	@GetMapping("/{productId}")
	public ResponseEntity<ProductResponse> getActiveProduct(@PathVariable UUID productId) {
		ProductResponse res = productService.getActiveProductDetail(productId);
		return ResponseEntity.ok(res);
	}

	@GetMapping
	public ResponseEntity<Page<ProductListResponse>> getActiveProduct(ProductSearchCondition condition, Pageable pageable) {
		log.info("condition: {}", condition);
		Page<ProductListResponse> res = productService.getProductList(condition, pageable);
		log.info("res: {}", res);
		return ResponseEntity.ok(res);
	}

}
