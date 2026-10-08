package com.mymart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductStockOverviewDto {
    private String productId;
    private String productName;
    private String sku;
    private String categoryName;
    private int mainQuantity;
    private int storeQuantity;
    private int totalQuantity;
    private int lowStockThreshold;
    private boolean lowStock;
    private boolean outOfStock;
}
