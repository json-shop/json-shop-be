package deepdive.jsonstore.domain.admin.service.product;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import deepdive.jsonstore.common.s3.S3ImageService;
import deepdive.jsonstore.domain.admin.dto.CreateProductRequest;
import deepdive.jsonstore.domain.product.repository.ProductRepository;
import deepdive.jsonstore.domain.product.service.ProductValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminProductService {

	private final S3ImageService s3ImageService;
	private final ProductValidationService productValidationService;
	private final ProductRepository productRepository;

	public void createProduct(MultipartFile productImage, CreateProductRequest createProductRequest) {
		String image = s3ImageService.uploadImage(productImage);
		productRepository.save(createProductRequest.toProduct(image));
	}
}
