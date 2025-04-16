package deepdive.jsonstore.domain.product.service;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import deepdive.jsonstore.domain.product.dto.ProductOrderCountDTO;
import deepdive.jsonstore.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductScheduler {

	private final ProductRepository productRepository;

	@Scheduled(cron = "0 0/5 * * * ?")
	@Transactional
	public void updateSoldCount() {
		List<ProductOrderCountDTO> productDTOList = productRepository.findSoldCount();
		productDTOList.forEach(productDTO -> {
			productDTO.product().updateSoldCount(productDTO.count());
		});
	}

}
