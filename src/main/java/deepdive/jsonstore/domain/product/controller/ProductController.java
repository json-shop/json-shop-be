package deepdive.jsonstore.domain.product.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import deepdive.jsonstore.domain.product.dto.ActiveProductResponse;
import deepdive.jsonstore.domain.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@RestController
public class ProductController {
	private final ProductService productService;

	@GetMapping("/{productId}")
	public ResponseEntity<ActiveProductResponse> getActiveProduct(@PathVariable UUID productId) {
		ActiveProductResponse res = productService.getActiveProductDetail(productId);
		return ResponseEntity.ok(res);
	}

}
