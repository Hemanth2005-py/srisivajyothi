package com.mymart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductFilterCriteria {
    private String query;
    private String categoryId;
    private String brandId;
    private Double minPrice;
    private Double maxPrice;
    private Boolean discountOnly;
    private Boolean inStockOnly;
    private String sort; // price_asc, price_desc, newest, popular
}
