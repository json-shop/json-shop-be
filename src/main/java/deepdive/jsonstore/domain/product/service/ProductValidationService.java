package deepdive.jsonstore.domain.product.service;

import java.util.UUID;

import de.huxhorn.sulky.ulid.ULID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import deepdive.jsonstore.domain.product.entity.ProductStatus;
import deepdive.jsonstore.domain.product.exception.ProductException;
import deepdive.jsonstore.domain.product.entity.Product;
import deepdive.jsonstore.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductValidationService {

	private final ProductRepository productRepository;


	public Product findActiveProductById(UUID id) {
		return productRepository.findByUidAndStatusIsNot(id, ProductStatus.DISCONTINUED)
			.orElseThrow(ProductException.ProductNotFoundException::new);
	}

	public Product findProductByIdAndAdmin(UUID productId, UUID adminId) {

		Product product = productRepository.findByUid(productId).orElseThrow(ProductException.ProductNotFoundException::new);
		if(!product.getAdmin().getUid().equals(adminId)) throw new ProductException.ProductForbiddenException();
		return product;
	}
}
