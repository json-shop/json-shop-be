package deepdive.jsonstore.domain.product.entity;


import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(indexName = "products")
@Getter
@Builder
public class ProductDocument {
    @Id
    @Field(type = FieldType.Keyword)
    private byte[] id;

    @Field(type = FieldType.Text)
    private String name;

    @Field(type = FieldType.Text)
    private String description;

    @Field(type = FieldType.Integer)
    private int price;

    @Field(type = FieldType.Integer)
    private int stock;

    @Field(type = FieldType.Text)
    private String image;
    @Field(type = FieldType.Text)
    private Category category;
    @Field(type = FieldType.Text)
    private ProductStatus status;

    @Field(type = FieldType.Date)
    private LocalDateTime createdAt;
    @Field(type = FieldType.Date)
    private LocalDateTime updatedAt;


    @Field(type = FieldType.Nested)
    private AdminInfo adminInfo;


    @Getter
    @Builder
    public static class AdminInfo{
        @Field(type = FieldType.Keyword)
        private Long id;
        @Field(type = FieldType.Keyword)
        private byte[] ulid;
    }


    public static ProductDocument from(Product product){
        return ProductDocument.builder()
                .id(product.getUlid())
                .name(product.getName())
                .description(product.getDetail())
                .price(product.getPrice())
                .stock(product.getStock())
                .image(product.getImage())
                .category(product.getCategory())
                .status(product.getStatus())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .adminInfo(AdminInfo.builder()
                        .id(product.getAdmin().getId())
                        .ulid(product.getAdmin().getUlid())
                        .build())
                .build();
    }
}


