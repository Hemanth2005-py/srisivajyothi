package com.mymart.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "products")
@CompoundIndexes({
        @CompoundIndex(name = "name_category_idx", def = "{'name': 1, 'categoryId': 1}")
})
public class Product {
    @Id
    private String id;

    @Indexed
    private String name;

    @Indexed
    private String brandId;

    @Indexed
    private String categoryId;

    private String description;
    private Double weight;
    private String unit;

    private Double originalPrice;
    private Double discountPrice;

    @Indexed(unique = true)
    private String sku;

    private String imageUrl;

    @Builder.Default
    private List<String> imageUrls = new ArrayList<>();

    @Builder.Default
    private boolean active = true;

    @Builder.Default
    private boolean featured = false;

    @Builder.Default
    private boolean bestseller = false;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public Double getEffectivePrice() {
        return (discountPrice != null && discountPrice > 0 && discountPrice <= originalPrice)
                ? discountPrice
                : originalPrice;
    }

    public int getDiscountPercentage() {
        if (originalPrice != null && originalPrice > 0 && discountPrice != null && discountPrice < originalPrice) {
            return (int) Math.round(((originalPrice - discountPrice) / originalPrice) * 100);
        }
        return 0;
    }
}
