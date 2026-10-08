package com.mymart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandSalesDto {
    private String brandId;
    private String brandName;
    private Double salesAmount;
    private int orderCount;
}
