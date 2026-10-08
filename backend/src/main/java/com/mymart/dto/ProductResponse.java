package com.mymart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private String id;
    private String name;
    private String brandId;
    private String brandName;
    private String categoryId;
    private String categoryName;
    private String description;
    private Double weight;
    private String unit;
    private Double originalPrice;
    private Double discountPrice;
    private Double effectivePrice;
    private int discountPercentage;
    private String sku;
    private String imageUrl;
    private List<String> imageUrls;
    private boolean active;
    private boolean featured;
    private boolean bestseller;
    private int stockMain;
    private int stockStore;
    private int totalStock;
    private boolean inStock;
    private Instant createdAt;
    private Instant updatedAt;
}
