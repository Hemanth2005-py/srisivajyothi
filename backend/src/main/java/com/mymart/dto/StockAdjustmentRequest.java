package com.mymart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAdjustmentRequest {
    @NotBlank(message = "Product ID is required")
    private String productId;

    @NotBlank(message = "Location is required (must be 'Main' or 'store')")
    private String location;

    @Min(value = 0, message = "New quantity cannot be negative")
    private int newQuantity;

    private String reason;
}
