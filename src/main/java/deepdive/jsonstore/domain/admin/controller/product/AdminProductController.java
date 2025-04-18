package deepdive.jsonstore.domain.admin.controller.product;

import java.net.URI;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import deepdive.jsonstore.domain.admin.dto.AdminProductListResponse;
import deepdive.jsonstore.domain.admin.dto.AdminProductResponse;
import deepdive.jsonstore.domain.admin.dto.CreateProductRequest;
import deepdive.jsonstore.domain.admin.dto.UpdateProductRequest;
import deepdive.jsonstore.domain.admin.service.product.AdminProductService;
import deepdive.jsonstore.domain.auth.entity.AdminMemberDetails;
import deepdive.jsonstore.domain.product.dto.ProductListResponse;
import deepdive.jsonstore.domain.product.dto.ProductResponse;
import deepdive.jsonstore.domain.product.dto.ProductSearchCondition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
@RestController
public class AdminProductController {

	private final AdminProductService adminProductService;

	@PostMapping
	public ResponseEntity<Void> createProduct(@RequestPart(value = "image", required = false) MultipartFile productImage,
											  @AuthenticationPrincipal AdminMemberDetails admin,
											  @RequestPart("productRequest") CreateProductRequest createProductRequest) {
		if (productImage == null || productImage.isEmpty()) {
			log.info("in this");
			String id = adminProductService.createProduct(admin.getAdminUid(), createProductRequest);
			return ResponseEntity.created(URI.create("/api/v1/products/"+id)).build();
		}

		String id = adminProductService.createProduct(admin.getAdminUid(), productImage, createProductRequest);
		return ResponseEntity.created(URI.create("/api/v1/products/"+id)).build();
	}

	@PutMapping
	public ResponseEntity<ProductResponse> updateProduct(@RequestPart("image") MultipartFile productImage,
		@AuthenticationPrincipal AdminMemberDetails admin,
		@RequestPart("productRequest") UpdateProductRequest updateProductRequest) {
		ProductResponse res = adminProductService.updateProduct(admin.getAdminUid(), productImage, updateProductRequest);
		return ResponseEntity.ok().body(res);
	}

	@GetMapping
	public ResponseEntity<Page<AdminProductListResponse>> getAdminProductList(@RequestParam String adminId,
		ProductSearchCondition condition, Pageable pageable) {
		Page<AdminProductListResponse> res = adminProductService.getAdminProductList(UUID.fromString(adminId), condition, pageable);
		return ResponseEntity.ok(res);
	}

	@GetMapping("/{productId}")
	public ResponseEntity<AdminProductResponse> getAdminProduct(@PathVariable UUID productId, @RequestParam String adminId) {
		AdminProductResponse res = adminProductService.getAdminProduct(UUID.fromString(adminId), productId);
		return ResponseEntity.ok(res);
	}

}
