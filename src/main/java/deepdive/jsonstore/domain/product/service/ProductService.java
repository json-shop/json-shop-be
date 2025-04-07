package deepdive.jsonstore.domain.product.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import deepdive.jsonstore.domain.product.dto.ActiveProductResponse;
import deepdive.jsonstore.domain.admin.dto.CreateProductRequest;
import deepdive.jsonstore.domain.product.entity.Product;
import deepdive.jsonstore.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

	private final ProductValidationService productValidationService;
	private final ProductRepository productRepository;

	@Transactional(readOnly = true)
	public ActiveProductResponse getActiveProductDetail(UUID id) {
		Product product = productValidationService.findActiveProductById(id);
		return ActiveProductResponse.toActiveProductResponse(product);
	}

	public void tempSave(List<CreateProductRequest> createProductRequestList) {
		createProductRequestList.forEach(p -> productRepository.save(p.toProduct("test.jpg")));
	}
}
