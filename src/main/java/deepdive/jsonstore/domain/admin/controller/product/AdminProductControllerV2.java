package deepdive.jsonstore.domain.admin.controller.product;

import de.huxhorn.sulky.ulid.ULID;
import deepdive.jsonstore.common.util.UlidUtil;
import deepdive.jsonstore.domain.admin.dto.AdminProductListResponse;
import deepdive.jsonstore.domain.admin.dto.AdminProductResponse;
import deepdive.jsonstore.domain.admin.dto.CreateProductRequest;
import deepdive.jsonstore.domain.admin.dto.UpdateProductRequest;
import deepdive.jsonstore.domain.admin.service.product.AdminProductService;
import deepdive.jsonstore.domain.admin.service.product.AdminProductServiceV2;
import deepdive.jsonstore.domain.auth.entity.AdminMemberDetails;
import deepdive.jsonstore.domain.product.dto.ProductResponse;
import deepdive.jsonstore.domain.product.dto.ProductSearchCondition;
import deepdive.jsonstore.domain.product.entity.Product;
import deepdive.jsonstore.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequestMapping("/api/v2/admin/products")
@RequiredArgsConstructor
@RestController
public class AdminProductControllerV2 {

	private final AdminProductServiceV2 adminProductService;

	/**
	 * 상품 등록
	 * @param productImage
	 * @param admin
	 * @param createProductRequest
	 * @return
	 */
	@PostMapping
	public ResponseEntity<Void> createProduct(@RequestPart(value = "image", required = false) MultipartFile productImage,
											  @AuthenticationPrincipal AdminMemberDetails admin,
											  @RequestPart("productRequest") CreateProductRequest createProductRequest) {
		log.info("productImage.isEmpty(): {}", productImage.isEmpty());
		log.info("CreateProductRequest: {}", createProductRequest);
		if (productImage == null || productImage.isEmpty()) {


			String id = adminProductService.createProduct(admin.getAdminUid(), createProductRequest);
			return ResponseEntity.created(URI.create("/api/v2/products/"+id)).build();
		}

		String id = adminProductService.createProduct(admin.getAdminUid(), productImage, createProductRequest);
		return ResponseEntity.created(URI.create("/api/v2/products/"+id)).build();
	}

	/**
	 * 상품 수정
	 * @param productImage
	 * @param admin
	 * @param updateProductRequest
	 * @return
	 */
	@PutMapping
	public ResponseEntity<ProductResponse> updateProduct(@RequestPart(value = "image", required = false) MultipartFile productImage,
														 @AuthenticationPrincipal AdminMemberDetails admin,
														 @RequestPart("productRequest") UpdateProductRequest updateProductRequest) {
		log.info("UpdateProductRequest: {}", updateProductRequest);
		ProductResponse res = adminProductService.updateProduct(admin.getAdminUid(), productImage, updateProductRequest);
		return ResponseEntity.ok().body(res);
	}

	@GetMapping
	public ResponseEntity<Page<AdminProductListResponse>> getAdminProductList(
			@AuthenticationPrincipal(expression = "adminUid") UUID adminUid,
			ProductSearchCondition condition, Pageable pageable
	) {
		Page<AdminProductListResponse> res = adminProductService.getAdminProductList(adminUid, condition, pageable);
		return ResponseEntity.ok(res);
	}

	@GetMapping("/{productId}")
	public ResponseEntity<AdminProductResponse> getAdminProduct(
			@AuthenticationPrincipal(expression = "adminUid") UUID adminUid,
			@PathVariable UUID productId
	) {
		AdminProductResponse res = adminProductService.getAdminProduct(adminUid, productId);
		return ResponseEntity.ok(res);
	}

//	@GetMapping("/test")
//	public ResponseEntity<String> test() {
//		byte[] ulidBytes = UlidUtil.createUlidBytes();
//		ULID.Value value = new ULID().nextValue();
//		String string = value.toString();
//
//		Product build = Product.builder()
//				.uid(UUID.randomUUID())
//				.ulid(ulidBytes)
//				.build();
//
//		Product save = productRepository.save(build);
//
//		byte[] ulid = save.getUlid();
//		String string1 = ulid.toString();
//		return ResponseEntity.ok(string);
//	}
}
