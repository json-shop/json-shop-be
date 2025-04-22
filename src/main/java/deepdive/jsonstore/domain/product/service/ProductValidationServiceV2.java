package deepdive.jsonstore.domain.product.service;

import de.huxhorn.sulky.ulid.ULID;
import deepdive.jsonstore.domain.product.entity.Product;
import deepdive.jsonstore.domain.product.entity.ProductStatus;
import deepdive.jsonstore.domain.product.exception.ProductException;
import deepdive.jsonstore.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductValidationServiceV2 {

	private final ProductRepository productRepository;

	public Product findActiveProductById(UUID id) {
		return productRepository.findByUidAndStatusIsNot(id, ProductStatus.DISCONTINUED)
			.orElseThrow(ProductException.ProductNotFoundException::new);
	}

	public Product findProductByIdAndAdmin(UUID productId, UUID adminId) {
		Product product = productRepository.findByUlid(productId).orElseThrow(ProductException.ProductNotFoundException::new);
		if(!product.getAdmin().getUid().equals(adminId)) throw new ProductException.ProductForbiddenException();
		return product;
	}
}
