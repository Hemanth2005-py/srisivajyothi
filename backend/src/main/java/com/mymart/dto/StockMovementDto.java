package com.mymart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockMovementDto {
    private String id;
    private String productId;
    private String productName;
    private String sku;
    private String sourceLocation;
    private String destinationLocation;
    private int quantity;
    private String movementType;
    private String reason;
    private String performedBy;
    private Instant createdAt;
}
