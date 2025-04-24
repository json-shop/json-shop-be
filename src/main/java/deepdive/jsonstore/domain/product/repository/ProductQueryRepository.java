package deepdive.jsonstore.domain.product.repository;

import static deepdive.jsonstore.domain.product.entity.QProduct.*;

import java.util.List;
import java.util.UUID;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import deepdive.jsonstore.domain.admin.dto.AdminProductListResponse;
import deepdive.jsonstore.domain.admin.exception.AdminException;
import deepdive.jsonstore.domain.product.dto.ProductListResponse;
import deepdive.jsonstore.domain.product.dto.ProductSearchCondition;
import deepdive.jsonstore.domain.product.dto.ProductSortType;
import deepdive.jsonstore.domain.product.entity.Category;
import deepdive.jsonstore.domain.product.entity.ProductStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ProductQueryRepository {

	private final JPAQueryFactory queryFactory;

	public Page<ProductListResponse> searchProductList(ProductSearchCondition condition, Pageable pageable) {
		OrderSpecifier<?> orderSpecifier = getOrderSpecifier(condition).nullsLast();

		List<ProductListResponse> content = queryFactory
			.select(Projections.constructor(ProductListResponse.class,
				product.uid,
				product.name,
				product.image,
				product.category,
				product.price,
				product.status,
				product.createdAt))
			.from(product)
			.where(categoryEq(condition.category()), searchContains(condition.search()), statusStopNe())
			.orderBy(orderSpecifier)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		Long totalCount = queryFactory
			.select(product.count())
			.from(product)
			.where(categoryEq(condition.category()), searchContains(condition.search()))
			.fetchOne();
		long total = totalCount != null ? totalCount : 0L;

		return new PageImpl<>(content, pageable, total);
	}
	public Page<AdminProductListResponse> searchAdminProductList(UUID uid, ProductSearchCondition condition, Pageable pageable) {
		OrderSpecifier<?> orderSpecifier = getOrderSpecifier(condition).nullsLast();

		List<AdminProductListResponse> content = queryFactory
				.select(Projections.constructor(AdminProductListResponse.class,
						product.uid,
						product.ulid,
						product.name,
						product.image,
						product.category,
						product.price,
						product.stock,
						product.status,
						product.soldCount,
						product.createdAt))
				.from(product)
				.where(categoryEq(condition.category()), searchContains(condition.search()), adminEq(uid))
				.orderBy(orderSpecifier)
				.offset(pageable.getOffset())
				.limit(pageable.getPageSize())
				.fetch();

		Long totalCount = queryFactory
				.select(product.count())
				.from(product)
				.where(categoryEq(condition.category()), searchContains(condition.search()))
				.fetchOne();
		long total = totalCount != null ? totalCount : 0L;

		return new PageImpl<>(content, pageable, total);
	}


	public Page<AdminProductListResponse> searchAdminProductList(byte[] ulid, ProductSearchCondition condition, Pageable pageable) {
		OrderSpecifier<?> orderSpecifier = getOrderSpecifier(condition).nullsLast();

		List<AdminProductListResponse> content = queryFactory
			.select(Projections.constructor(AdminProductListResponse.class,
				product.uid,
				product.ulid,
				product.name,
				product.image,
				product.category,
				product.price,
				product.stock,
				product.status,
				product.soldCount,
				product.createdAt))
			.from(product)
			.where(categoryEq(condition.category()), searchContains(condition.search()), ulidEq(ulid))
			.orderBy(orderSpecifier)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		Long totalCount = queryFactory
			.select(product.count())
			.from(product)
			.where(categoryEq(condition.category()), searchContains(condition.search()))
			.fetchOne();
		long total = totalCount != null ? totalCount : 0L;

		return new PageImpl<>(content, pageable, total);
	}

	private OrderSpecifier<?> getOrderSpecifier(ProductSearchCondition condition) {
		ProductSortType sortType = condition.sort() == null ? ProductSortType.LATEST : condition.sort();
		return switch (sortType) {
			case LOW_PRICE -> product.price.asc();
			case HIGH_PRICE -> product.price.desc();
			case SALES -> product.soldCount.desc();
			default -> product.createdAt.desc();
		};
	}

	private BooleanExpression categoryEq(Category category) {
		return category == null ? null : product.category.eq(category);
	}

	private BooleanExpression searchContains(String search) {
		return (search == null || search.trim().isEmpty()) ? null : product.name.containsIgnoreCase(search.trim());
	}

	private BooleanExpression statusStopNe() {
		return product.status.ne(ProductStatus.DISCONTINUED);
	}

	private BooleanExpression adminEq(UUID adminUid) {
		if (adminUid == null) throw new AdminException.AdminBadRequestException();
		return product.admin.uid.eq(adminUid);
	}

	private BooleanExpression ulidEq(byte[] ulid) {
		return (ulid == null || ulid.length == 0)
				? null
				: product.ulid.eq(ulid);
	}

}
