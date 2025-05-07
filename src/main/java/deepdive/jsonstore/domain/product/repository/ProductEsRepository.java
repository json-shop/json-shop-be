package deepdive.jsonstore.domain.product.repository;

import deepdive.jsonstore.domain.product.entity.Category;
import deepdive.jsonstore.domain.product.entity.ProductDocument;
import deepdive.jsonstore.domain.product.entity.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;
import java.util.Optional;

public interface ProductEsRepository extends ElasticsearchRepository<ProductDocument, byte[]> {
    List<ProductDocument> findByName(String name);
    List<ProductDocument> findByNameContaining(String name);
    Optional<ProductDocument> findByIdAndStatusIsNot(byte[] id, ProductStatus status);

    @Query("""
    {
      "bool": {
        "must": [
          { "term": { "category": "?0" }},
          { "match": { "name":     "?1" }}
        ],
        "must_not": [
          { "term": { "status": "DISCONTINUED" }}
        ]
      }
    }
    """)
    Page<ProductDocument> searchByCategoryAndName(
            Category category,
            String name,
            Pageable pageable
    );


}
