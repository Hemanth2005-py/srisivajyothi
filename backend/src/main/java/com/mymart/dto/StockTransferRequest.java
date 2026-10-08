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
public class StockTransferRequest {
    @NotBlank(message = "Product ID is required")
    private String productId;

    @NotBlank(message = "Source location is required (Main or store)")
    private String fromLocation;

    @NotBlank(message = "Destination location is required (Main or store)")
    private String toLocation;

    @Min(value = 1, message = "Transfer quantity must be at least 1")
    private int quantity;

    private String reason;
}
