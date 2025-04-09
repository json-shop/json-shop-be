package deepdive.jsonstore.domain.admin.controller.product;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import deepdive.jsonstore.domain.admin.dto.CreateProductRequest;
import deepdive.jsonstore.domain.admin.dto.UpdateProductRequest;
import deepdive.jsonstore.domain.admin.service.product.AdminProductService;
import deepdive.jsonstore.domain.product.dto.ProductResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
@RestController
public class AdminProductController {

	private final AdminProductService adminProductService;

	@PostMapping
	public ResponseEntity<Void> createProduct(@RequestPart("image") MultipartFile productImage,
		@RequestPart("id") String adminId, //TODO 임시로 id받음
		@RequestPart("productRequest") CreateProductRequest createProductRequest) {
		String id = adminProductService.createProduct(UUID.fromString(adminId), productImage, createProductRequest);
		return ResponseEntity.created(URI.create("/api/v1/products/"+id)).build();
	}

	@PutMapping
	public ResponseEntity<ProductResponse> updateProduct(@RequestPart("image") MultipartFile productImage,
		@RequestPart("id") String adminId, //TODO 임시로 id받음
		@RequestPart("productRequest") UpdateProductRequest updateProductRequest) {
		ProductResponse res = adminProductService.updateProduct(UUID.fromString(adminId), productImage, updateProductRequest);
		return ResponseEntity.ok().body(res);
	}

	@PostMapping("/temp")
	public ResponseEntity<Void> createAdmin() {
		adminProductService.tempSave();
		return ResponseEntity.noContent().build();
	}
}
