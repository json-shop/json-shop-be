package deepdive.jsonstore.domain.product.entity;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Category {
	FOOD("식품"), BEAUTY("뷰티"), DAILY("생활용품"), TOY("완구"), HEALTH("헬스");

	private final String name;
}
