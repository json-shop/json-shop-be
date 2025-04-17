package deepdive.jsonstore.domain.product.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import deepdive.jsonstore.domain.admin.entity.Admin;
import deepdive.jsonstore.domain.admin.repository.AdminRepository;
import deepdive.jsonstore.domain.order.entity.Order;
import deepdive.jsonstore.domain.order.entity.OrderProduct;
import deepdive.jsonstore.domain.order.repository.OrderProductRepository;
import deepdive.jsonstore.domain.order.repository.OrderRepository;
import deepdive.jsonstore.domain.product.dto.ProductListResponse;
import deepdive.jsonstore.domain.product.dto.ProductResponse;
import deepdive.jsonstore.domain.product.dto.ProductSearchCondition;
import deepdive.jsonstore.domain.product.entity.Category;
import deepdive.jsonstore.domain.product.entity.Product;
import deepdive.jsonstore.domain.product.entity.ProductStatus;
import deepdive.jsonstore.domain.product.repository.ProductQueryRepository;
import deepdive.jsonstore.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

	private final ProductValidationService productValidationService;
	private final ProductQueryRepository productQueryRepository;
	private final ProductRepository productRepository;
	private final AdminRepository adminRepository;
	private final OrderProductRepository orderProductRepository;
	private final OrderRepository orderRepository;

	public ProductResponse getActiveProductDetail(UUID id) {
		Product product = productValidationService.findActiveProductById(id);
		return ProductResponse.toProductResponse(product);
	}

	public Page<ProductListResponse> getProductList(ProductSearchCondition condition, Pageable pageable) {
		return productQueryRepository.searchProductList(condition, pageable);
	}

	public Page<ProductListResponse> test(ProductSearchCondition condition, Pageable pageable) {
		return productQueryRepository.test(condition, pageable);
	}

	@Transactional
	public void temp() {
		List<Product> productList = new ArrayList<>();
		Admin admin = adminRepository.findByEmail("admin1@example.com").orElseThrow();
		for(int i = 11; i < 100000; i++) {
			productList.add(Product.builder()
				.uid(UUID.randomUUID())
				.image("test")
				.name("상품"+i)
				.price(10000)
				.admin(admin)
				.stock(20)
				.category(Category.HEALTH)
				.status(ProductStatus.ON_SALE)
				.detail("상품설명"+i)
				.soldCount(0)
				.build());
		}
		productRepository.saveAll(productList);
	}

	@Transactional
	public void tempOrder() {
		List<Product> productList = productRepository.findAll();
		Order order = orderRepository.findByUid(UUID.fromString("3f15ae9c-4bd9-4f05-90c1-459d9c9dddbd")).orElseThrow();
		List<OrderProduct> orderList = new ArrayList<>();
		for(int i = 11; i < 10000; i++) {
			orderList.add(OrderProduct.from(order, find(productList), random(1,10)));
		}
		orderProductRepository.saveAll(orderList);
	}

	public int random(int min, int max) {
		Random random = new Random();
		return random.nextInt(max - min + 1) + min;

	}

	public Product find(List<Product> productList) {
		List<Product> product = productList.stream().filter(p -> p.getId() == random(99997, 199985)).toList();
		while(product.isEmpty()) {
			product = productList.stream().filter(p -> p.getId() == random(99997, 199985)).toList();
		}
		return product.getFirst();
	}

}
