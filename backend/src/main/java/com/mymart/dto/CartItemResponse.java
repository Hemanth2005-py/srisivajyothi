package com.mymart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {
    private String productId;
    private String productName;
    private String sku;
    private String brandName;
    private String imageUrl;
    private Double weight;
    private String unit;
    private Double originalPrice;
    private Double discountPrice;
    private Double effectivePrice;
    private int discountPercentage;
    private int quantity;
    private Double subtotal;
    private int availableStock;
}
