package deepdive.jsonstore.domain.product.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import deepdive.jsonstore.domain.product.dto.ActiveProductResponse;
import deepdive.jsonstore.domain.product.entity.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

	private final ProductValidationService productValidationService;

	public ActiveProductResponse getActiveProductDetail(UUID id) {
		Product product = productValidationService.findActiveProductById(id);
		return product.toActiveProductResponse();
	}
}
