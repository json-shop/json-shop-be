package deepdive.jsonstore.domain.admin.service.product;

import deepdive.jsonstore.common.s3.S3ImageService;
import deepdive.jsonstore.common.util.UlidUtil;
import deepdive.jsonstore.domain.admin.dto.*;
import deepdive.jsonstore.domain.admin.entity.Admin;
import deepdive.jsonstore.domain.admin.repository.AdminRepository;
import deepdive.jsonstore.domain.admin.service.AdminValidationService;
import deepdive.jsonstore.domain.admin.service.product.evnet.ProductCreatedEvent;
import deepdive.jsonstore.domain.admin.service.product.evnet.ProductDeletedEvent;
import deepdive.jsonstore.domain.admin.service.product.evnet.ProductUpdatedEvent;
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
import org.hibernate.validator.internal.constraintvalidators.bv.size.SizeValidatorForArraysOfLong;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;

import static java.util.Base64.getEncoder;
import static java.util.Base64.getUrlEncoder;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminProductServiceV2 {

	private final S3ImageService s3ImageService;
	private final ProductValidationServiceV2 productValidationService;
	private final AdminValidationService adminValidationService;
	private final ProductRepository productRepository;
	private final ProductQueryRepository productQueryRepository;
	private final ApplicationEventPublisher eventPublisher;

	@Transactional
	public String createProduct(byte[] adminUid, MultipartFile productImage, CreateProductRequest createProductRequest) {
		Admin admin = adminValidationService.getAdminById(adminUid);
		String image = s3ImageService.uploadImage(productImage);
		Product product = productRepository.save(createProductRequest.toProduct(image,admin,getImageByte(productImage)));

		eventPublisher.publishEvent(new ProductCreatedEvent(product));
		return getEncoder().encodeToString(product.getUlid());

	}

	@Transactional
	public String createProduct(byte[] adminUid, CreateProductRequest createProductRequest) {
		Admin admin = adminValidationService.getAdminById(adminUid);
		Product product = productRepository.save(createProductRequest.toProduct(null, admin, null));


		eventPublisher.publishEvent(new ProductCreatedEvent(product));
		return getUrlEncoder().encodeToString(product.getUlid());
	}




	@Transactional
	public ProductResponse updateProduct(byte[] adminUid, MultipartFile productImage, UpdateProductRequest updateProductRequest) {

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
		eventPublisher.publishEvent(new ProductUpdatedEvent(product));
		return ProductResponse.toProductResponse(product);
	}

	public Page<AdminProductListResponseWithSafeUlid> getAdminProductList(byte[] adminUlid, ProductSearchCondition productSearchCondition, Pageable pageable) {
		Page<AdminProductListResponse> page = productQueryRepository.searchAdminProductList(adminUlid, productSearchCondition, pageable);

		return page.map(r -> {
			String safeUlid = getUrlEncoder().encodeToString(r.ulid());
			return AdminProductListResponseWithSafeUlid.builder()
					.uid(r.uid())
					.ulid(safeUlid)
					.productName(r.productName())
					.image(r.image())
					.category(r.category())
					.price(r.price())
					.stock(r.stock())
					.status(r.status())
					.soldCount(r.soldCount())
					.createdAt(r.createdAt()).build();

		});
	}
	public AdminProductResponse getAdminProduct(byte[] adminUid, String productUid) {

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


	@Transactional
	public void deleteProduct(byte[] adminUlid, String productId) {
		Product product = productValidationService.deleteProductByIdAndAdmin(productId, adminUlid);
		log.info("delete product: {}", productId);
		eventPublisher.publishEvent(new ProductDeletedEvent(product));
	}
}
