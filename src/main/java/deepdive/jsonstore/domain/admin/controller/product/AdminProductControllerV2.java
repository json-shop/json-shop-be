package deepdive.jsonstore.domain.admin.controller.product;

import de.huxhorn.sulky.ulid.ULID;
import deepdive.jsonstore.common.util.UlidUtil;
import deepdive.jsonstore.domain.admin.dto.*;
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
import java.util.Arrays;
import java.util.Base64;
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


			String id = adminProductService.createProduct(admin.getAdminUlid(), createProductRequest);
			log.info("상품 추가 - id: {}", id);


			return ResponseEntity.created(URI.create("/api/v2/products/"+ id)).build();
		}

		String id = adminProductService.createProduct(admin.getAdminUlid(), productImage, createProductRequest);
		log.info("상품 추가 - id: {}", id);
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

		ProductResponse res = adminProductService.updateProduct(admin.getAdminUlid(), productImage, updateProductRequest);
		return ResponseEntity.ok().body(res);
	}

	/**
	 * 상품 리스트 조회
	 * @param adminUlid
	 * @param condition
	 * @param pageable
	 * @return
	 */
	@GetMapping
	public ResponseEntity<Page<AdminProductListResponseWithSafeUlid>> getAdminProductList(
			@AuthenticationPrincipal(expression = "adminUlid") byte[] adminUlid,
			ProductSearchCondition condition, Pageable pageable
	) {
		Page<AdminProductListResponseWithSafeUlid> res = adminProductService.getAdminProductList(adminUlid, condition, pageable);
		return ResponseEntity.ok(res);
	}

	@GetMapping("/{productId}")
	public ResponseEntity<AdminProductResponse> getAdminProduct(
			@AuthenticationPrincipal(expression = "adminUlid") byte[] adminUid,
			@PathVariable String productId
	) {
		AdminProductResponse res = adminProductService.getAdminProduct(adminUid, productId);
		return ResponseEntity.ok(res);
	}

	@DeleteMapping("/{productId}")
	public ResponseEntity<Void> deleteProduct(
			@AuthenticationPrincipal(expression = "adminUlid") byte[] adminUlid,
			@PathVariable String productId
	){
		log.info("상품 삭제 요청: {}", productId);
		adminProductService.deleteProduct(adminUlid, productId);
		return ResponseEntity.ok().build();
	}


//	@GetMapping("/test")
//	public ResponseEntity<byte[]> test(@RequestParam String ulid) {
//		//26자 문자열
//		log.info("ulid: {}", ulid);
//
//		byte[] ulidBytes = Base64.getDecoder().decode(ulid);
//
//		// 3) 실제 byte 배열 값 확인 (e.g. [1, 147, 117, ... ])
//		log.info("ulid bytes: {}", Arrays.toString(ulidBytes));
////		log.info("ulid: {}", Base64.getEncoder().encode(ulid));
////		log.info("decode: {}", Base64.getEncoder().encodeToString(ulid));
//		return ResponseEntity.ok(ulidBytes);
//	}
}
