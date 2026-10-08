package com.mymart.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {
    @NotBlank(message = "Product name is required")
    private String name;

    @NotBlank(message = "Brand ID is required")
    private String brandId;

    @NotBlank(message = "Category ID is required")
    private String categoryId;

    private String description;

    @NotNull(message = "Weight or volume is required")
    private Double weight;

    @NotBlank(message = "Unit of measurement is required (e.g. g, kg, ml, L, pack)")
    private String unit;

    @NotNull(message = "Original price is required")
    @DecimalMin(value = "0.01", message = "Original price must be greater than 0")
    private Double originalPrice;

    private Double discountPrice;

    @NotBlank(message = "SKU is required")
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

    private Integer initialMainStock;
    private Integer initialStoreStock;
}
