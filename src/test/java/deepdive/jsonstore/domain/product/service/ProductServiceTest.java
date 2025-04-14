package deepdive.jsonstore.domain.product.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import deepdive.jsonstore.domain.admin.entity.Admin;
import deepdive.jsonstore.domain.product.dto.ProductListResponse;
import deepdive.jsonstore.domain.product.dto.ProductResponse;
import deepdive.jsonstore.domain.product.dto.ProductSearchCondition;
import deepdive.jsonstore.domain.product.entity.Category;
import deepdive.jsonstore.domain.product.entity.Product;
import deepdive.jsonstore.domain.product.entity.ProductStatus;
import deepdive.jsonstore.domain.product.exception.ProductException;
import deepdive.jsonstore.domain.product.repository.ProductQueryRepository;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

	@Mock
	private ProductValidationService productValidationService;

	@Mock
	private ProductQueryRepository productQueryRepository;

	@InjectMocks
	private ProductService productService;

	private List<Product> productList;

	@BeforeEach
	public void setUp() {
		// 각 테스트 실행 전에 더미 데이터 리스트를 초기화
		productList = new ArrayList<>();
		Category[] categories = Category.values();

		for (int i = 1; i <= 5; i++) {
			UUID productId = UUID.randomUUID();
			Product product = Product.builder()
				.id((long)i)
				.soldCount(10*i)
				.price(10000*i)
				.uid(productId)
				.stock(10)
				.status(i%2 == 0 ?ProductStatus.ON_SALE: ProductStatus.DISCONTINUED)
				.name("상품"+i)
				.category(categories[i-1])
				.detail("상품설명")
				.admin(Admin.builder().username("test").build())
				.build();
			productList.add(product);
		}
	}

	@Test
	public void getActiveProductDetail_성공() {
		Product product = productList.getFirst();
		UUID uid = product.getUid();
		when(productValidationService.findActiveProductById(uid)).thenReturn(product);

		ProductResponse productResponse = productService.getActiveProductDetail(uid);
		ProductResponse expectedResponse = ProductResponse.toProductResponse(product);

		assertThat(productResponse).isEqualTo(expectedResponse);
	}

	@Test
	public void getActiveProductDetail_실패_ProductNotFoundException() {
		Product product = productList.get(2);
		UUID uid = product.getUid();

		when(productValidationService.findActiveProductById(uid))
			.thenThrow(ProductException.ProductNotFoundException.class);

		assertThatThrownBy(() -> productService.getActiveProductDetail(uid))
			.isInstanceOf(ProductException.ProductNotFoundException.class);
	}

	@Test
	public void getProductList_필터링_성공() {
		Product product = productList.getFirst();
		ProductSearchCondition searchCondition = ProductSearchCondition.builder()
			.category(product.getCategory()).build();
		Pageable pageable = PageRequest.of(0, 10);
		ProductListResponse productListResponse = ProductListResponse.toProductListResponse(product);

		Page<ProductListResponse> expectedPage = new PageImpl<>(Collections.singletonList(productListResponse), pageable, 1);
		when(productQueryRepository.searchProductList(searchCondition, pageable)).thenReturn(expectedPage);

		Page<ProductListResponse> actualPage = productService.getProductList(searchCondition, pageable);

		assertThat(actualPage).isNotNull();
		assertThat(actualPage.getTotalElements()).isEqualTo(1);
		assertThat(actualPage.getContent()).containsExactly(productListResponse);
	}

	@Test
	public void getProductList_검색어_성공() {
		ProductSearchCondition searchCondition = ProductSearchCondition.builder()
			.search("상품").build();
		Pageable pageable = PageRequest.of(0, 10);
		List<ProductListResponse> productListResponse = productList.stream()
			.map(ProductListResponse::toProductListResponse).toList();

		Page<ProductListResponse> expectedPage = new PageImpl<>(productListResponse, pageable, 5);
		when(productQueryRepository.searchProductList(searchCondition, pageable)).thenReturn(expectedPage);

		Page<ProductListResponse> actualPage = productService.getProductList(searchCondition, pageable);

		assertThat(actualPage).isNotNull();
		assertThat(actualPage.getTotalElements()).isEqualTo(5);
	}

}

