package deepdive.jsonstore.domain.product.repository;

import deepdive.jsonstore.domain.product.entity.ProductDocument;
import deepdive.jsonstore.domain.product.entity.ProductStatus;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;
import java.util.Optional;

public interface ProductEsRepository extends ElasticsearchRepository<ProductDocument, byte[]> {
    List<ProductDocument> findByName(String name);
    Optional<ProductDocument> findByIdAndStatusIsNot(byte[] id, ProductStatus status);
}
