package deepdive.jsonstore.domain.admin.service.product;

import deepdive.jsonstore.common.s3.S3ImageService;
import deepdive.jsonstore.common.util.UlidUtil;
import deepdive.jsonstore.domain.admin.dto.AdminProductListResponse;
import deepdive.jsonstore.domain.admin.dto.AdminProductResponse;
import deepdive.jsonstore.domain.admin.dto.CreateProductRequest;
import deepdive.jsonstore.domain.admin.dto.UpdateProductRequest;
import deepdive.jsonstore.domain.admin.entity.Admin;
import deepdive.jsonstore.domain.admin.repository.AdminRepository;
import deepdive.jsonstore.domain.admin.service.AdminValidationService;
import deepdive.jsonstore.domain.product.dto.ProductResponse;
import deepdive.jsonstore.domain.product.dto.ProductSearchCondition;
import deepdive.jsonstore.domain.product.entity.Product;
import deepdive.jsonstore.domain.product.repository.ProductQueryRepository;
import deepdive.jsonstore.domain.product.repository.ProductRepository;
import deepdive.jsonstore.domain.product.service.ProductValidationService;
import deepdive.jsonstore.domain.product.service.ProductValidationServiceV2;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminProductServiceV2 {

	private final S3ImageService s3ImageService;
	private final ProductValidationServiceV2 productValidationService;
	private final AdminValidationService adminValidationService;
	private final ProductRepository productRepository;
	private final ProductQueryRepository productQueryRepository;
	private final AdminRepository adminRepository;

	public String createProduct(UUID adminUid, MultipartFile productImage, CreateProductRequest createProductRequest) {
		Admin admin = adminValidationService.getAdminById(adminUid);
		String image = s3ImageService.uploadImage(productImage);
		Product product = productRepository.save(createProductRequest.toProduct(image,admin,getImageByte(productImage)));
		return product.getUid().toString();
	}

	public String createProduct(UUID adminUid, CreateProductRequest createProductRequest) {
		Admin admin = adminValidationService.getAdminById(adminUid);
		Product product = productRepository.save(createProductRequest.toProduct(null, admin, null));

		return product.getUid().toString();
	}

	@Transactional
	public ProductResponse updateProduct(UUID adminUid, MultipartFile productImage, UpdateProductRequest updateProductRequest) {
		Product product = productValidationService.findProductByIdAndAdmin(updateProductRequest.ulid(), adminUid);
		if(productImage != null && !productImage.isEmpty() ) {
			byte[] imageByte = getImageByte(productImage);
			if(!Arrays.equals(imageByte, product.getImageByte())) {
				s3ImageService.deleteImage(product.getImage());
				String image = s3ImageService.uploadImage(productImage);
				product.updateImage(image, imageByte);
			}
		}
		product.updateProduct(updateProductRequest);
		return ProductResponse.toProductResponse(product);
	}

	public Page<AdminProductListResponse> getAdminProductList(UUID adminUid, ProductSearchCondition productSearchCondition, Pageable pageable) {
		return productQueryRepository.searchAdminProductList(adminUid, productSearchCondition, pageable);
	}

	public AdminProductResponse getAdminProduct(UUID adminUid, UUID productUid) {
		Product product = productValidationService.findProductByIdAndAdmin(productUid, adminUid);
		return AdminProductResponse.toAdminProductResponse(product);
	}

	private byte[] getImageByte(MultipartFile productImage) {
		try {
			byte[] fileBytes = productImage.getBytes();
			MessageDigest md = MessageDigest.getInstance("MD5");
			return md.digest(fileBytes);
		} catch(IOException | NoSuchAlgorithmException e) {
			throw new RuntimeException("파일 업로드 중 오류가 발생했습니다.", e);
		}
	}
}
