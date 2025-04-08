package deepdive.jsonstore.domain.admin.service.product;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import deepdive.jsonstore.common.s3.S3ImageService;
import deepdive.jsonstore.domain.admin.dto.CreateProductRequest;
import deepdive.jsonstore.domain.admin.dto.UpdateProductRequest;
import deepdive.jsonstore.domain.admin.entity.Admin;
import deepdive.jsonstore.domain.admin.repository.AdminRepository;
import deepdive.jsonstore.domain.admin.service.AdminValidationService;
import deepdive.jsonstore.domain.product.entity.Product;
import deepdive.jsonstore.domain.product.repository.ProductRepository;
import deepdive.jsonstore.domain.product.service.ProductService;
import deepdive.jsonstore.domain.product.service.ProductValidationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminProductService {

	private final S3ImageService s3ImageService;
	private final ProductValidationService productValidationService;
	private final AdminValidationService adminValidationService;
	private final ProductRepository productRepository;
	private final AdminRepository adminRepository;

	public String createProduct(UUID adminUid, MultipartFile productImage, CreateProductRequest createProductRequest) {
		Admin admin = adminValidationService.getAdminById(adminUid);
		String image = s3ImageService.uploadImage(productImage);
		Product product = productRepository.save(createProductRequest.toProduct(image,admin));
		return product.getUid().toString();
	}

	@Transactional
	public void updateProduct(UUID adminUid, MultipartFile productImage, UpdateProductRequest updateProductRequest) {
		Product product = productValidationService.findProductByIdAndAdmin(updateProductRequest.uid(), adminUid);
		String image = updateProductRequest.image();
		if(image == null) image = s3ImageService.uploadImage(productImage);
		product.updateProduct(updateProductRequest, image);
	}

	@Transactional
	public void activateProduct(UUID adminUid, UUID productUid) {
		Product product = productValidationService.findProductByIdAndAdmin(productUid, adminUid);
		product.activate();
	}

	@Transactional
	public void deactivateProduct(UUID adminUid, UUID productUid) {
		Product product = productValidationService.findProductByIdAndAdmin(productUid, adminUid);
		product.deactivate();
	}

	public void tempSave() {
		adminRepository.save(Admin.builder()
			.username("admin")
			.password("temp")
			.email("tt@t.com")
			.deleted(false)
			.build());
	}
}
