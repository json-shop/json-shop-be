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

import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductValidationServiceV2 {

	private final ProductRepository productRepository;

	public Product findActiveProductById(String id) {
		return productRepository.findByUlidAndStatusIsNot(Base64.getUrlDecoder().decode(id), ProductStatus.DISCONTINUED)
			.orElseThrow(ProductException.ProductNotFoundException::new);
	}

	public Product findProductByIdAndAdmin(String ulid, byte[] adminId) {
		byte[] productId = Base64.getUrlDecoder().decode(ulid);
		Product product = productRepository.findByUlid(productId).orElseThrow(ProductException.ProductNotFoundException::new);
		if(!Arrays.equals(product.getAdmin().getUlid(), adminId)) throw new ProductException.ProductForbiddenException();
		return product;
	}

	public Product deleteProductByIdAndAdmin(String ulid, byte[] adminId) {
		byte[] productId = Base64.getUrlDecoder().decode(ulid);
		Product product = productRepository.findByUlid(productId).orElseThrow(ProductException.ProductNotFoundException::new);
		if(!Arrays.equals(product.getAdmin().getUlid(), adminId)) throw new ProductException.ProductForbiddenException();
		productRepository.delete(product);
		return product;
	}



}
