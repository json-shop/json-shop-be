package deepdive.jsonstore.domain.admin.service.product;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import deepdive.jsonstore.common.s3.S3ImageService;
import deepdive.jsonstore.domain.admin.dto.AdminProductListResponse;
import deepdive.jsonstore.domain.admin.dto.AdminProductResponse;
import deepdive.jsonstore.domain.admin.dto.CreateProductRequest;
import deepdive.jsonstore.domain.admin.dto.UpdateProductRequest;
import deepdive.jsonstore.domain.admin.entity.Admin;
import deepdive.jsonstore.domain.admin.service.AdminValidationService;
import deepdive.jsonstore.domain.product.dto.ProductResponse;
import deepdive.jsonstore.domain.product.dto.ProductSearchCondition;
import deepdive.jsonstore.domain.product.entity.Category;
import deepdive.jsonstore.domain.product.entity.Product;
import deepdive.jsonstore.domain.product.entity.ProductStatus;
import deepdive.jsonstore.domain.product.repository.ProductQueryRepository;
import deepdive.jsonstore.domain.product.repository.ProductRepository;
import deepdive.jsonstore.domain.product.service.ProductValidationService;

@ExtendWith(MockitoExtension.class)
class AdminProductServiceTest {

	@Mock
	private S3ImageService s3ImageService;

	@Mock
	private ProductValidationService productValidationService;

	@Mock
	private AdminValidationService adminValidationService;

	@Mock
	private ProductRepository productRepository;

	@Mock
	private ProductQueryRepository productQueryRepository;

	@InjectMocks
	private AdminProductService adminProductService;

	private List<Product> productList;
	private Admin admin;
	private MultipartFile mockFile;

	@BeforeEach
	public void setUp() throws NoSuchAlgorithmException {
		productList = new ArrayList<>();
		admin = Admin.builder().uid(UUID.randomUUID()).username("test").build();
		Category[] categories = Category.values();

		for (int i = 1; i <= 5; i++) {
			UUID productId = UUID.randomUUID();
			Product product = Product.builder()
				.id((long)i)
				.soldCount(10*i)
				.price(10000*i)
				.uid(productId)
				.stock(10)
				.status(i%2 == 0 ? ProductStatus.ON_SALE: ProductStatus.DISCONTINUED)
				.name("상품"+i)
				.category(categories[i-1])
				.detail("상품설명")
				.admin(admin)
				.build();
			productList.add(product);
		}

		mockFile = new MockMultipartFile("file", "test.jpg", "image/jpeg", "dummyImageData".getBytes());

		MessageDigest md = MessageDigest.getInstance("MD5");
		byte[] expectedImageByte = md.digest("dummyImageData".getBytes());

		productList.forEach(p -> p.updateImage("http://s3.image.url/test.png", expectedImageByte));
	}

	@Test
	void createProduct_성공() throws IOException {
		String imageUrl = "http://s3.image.url/test.png";
		CreateProductRequest createRequest = mock(CreateProductRequest.class);
		Product product = productList.getFirst();

		when(adminValidationService.getAdminById(admin.getUid())).thenReturn(admin);
		when(s3ImageService.uploadImage(mockFile)).thenReturn(imageUrl);
		when(createRequest.toProduct(imageUrl, admin, product.getImageByte())).thenReturn(product);
		when(productRepository.save(product)).thenReturn(product);

		String result = adminProductService.createProduct(admin.getUid(), mockFile, createRequest);

		assertThat(result).isEqualTo(product.getUid().toString());
	}

	@Test
	void updateProduct_성공() {
		Product product = productList.getFirst();
		UUID productUid = product.getUid();
		MultipartFile newImageFile = new MockMultipartFile("file", "newTest.jpg", "image/jpeg", "newDummyImageData".getBytes());
		String newImageUrl = "http://s3.image.url/new.png";
		byte[] newImageBytes = "newImage".getBytes();

		UpdateProductRequest updateRequest = UpdateProductRequest.builder()
			.productDetail(product.getDetail())
			.productName(product.getName())
			.category(product.getCategory())
			.price(product.getPrice())
			.stock(100)
			.uid(productUid)
			.status(product.getStatus())
			.build();

		when(productValidationService.findProductByIdAndAdmin(productUid, admin.getUid())).thenReturn(product);
		when(s3ImageService.uploadImage(newImageFile)).thenReturn(newImageUrl);

		ProductResponse result = adminProductService.updateProduct(admin.getUid(), newImageFile, updateRequest);

		product.updateProduct(updateRequest);
		product.updateImage(newImageUrl, newImageBytes);
		ProductResponse expectedResponse = ProductResponse.toProductResponse(product);


		assertThat(result).isEqualTo(expectedResponse);
		assertThat(result.stock()).isEqualTo(100);
		assertThat(result.image()).isEqualTo("http://s3.image.url/new.png");
		assertThat(newImageBytes).isEqualTo(product.getImageByte());
	}

	@Test
	void getAdminProductList() {
		ProductSearchCondition condition = mock(ProductSearchCondition.class);
		Pageable pageable = Pageable.unpaged();
		AdminProductListResponse dummyResponse = AdminProductListResponse.builder().build();
		Page<AdminProductListResponse> dummyPage = new PageImpl<>(Arrays.asList(dummyResponse));

		when(productQueryRepository.searchAdminProductList(admin.getUid(), condition, pageable)).thenReturn(dummyPage);
		Page<AdminProductListResponse> result = adminProductService.getAdminProductList(admin.getUid(), condition, pageable);
		assertThat(result).isNotNull();
		assertThat(result.getTotalElements()).isEqualTo(1);
	}

	@Test
	void getAdminProduct() {
		UUID productUid = productList.get(2).getUid();
		when(productValidationService.findProductByIdAndAdmin(productUid, admin.getUid())).thenReturn(productList.get(2));
		AdminProductResponse response = adminProductService.getAdminProduct(admin.getUid(), productUid);
		assertThat(response).isNotNull();
		assertThat(response.uid()).isEqualTo(productUid);
		assertThat(response.productName()).isEqualTo(productList.get(2).getName());
	}
}
