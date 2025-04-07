package deepdive.jsonstore.domain.product.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import deepdive.jsonstore.domain.product.entity.Product;
import deepdive.jsonstore.domain.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductValidationService {

	private final ProductRepository productRepository;

	public Product findActiveProductById(UUID id) {
		return productRepository.findByUidAndActiveIsTrue(id)
			.orElseThrow(() -> new EntityNotFoundException("존재하지 않는 상품입니다"));
	}
}
