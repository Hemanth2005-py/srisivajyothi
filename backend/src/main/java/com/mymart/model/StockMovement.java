package com.mymart.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "stockMovements")
public class StockMovement {
    @Id
    private String id;

    @Indexed
    private String productId;

    private String productName;
    private String sku;

    private String sourceLocation; // "Main", "store", or null
    private String destinationLocation; // "Main", "store", or null

    private int quantity;

    @Indexed
    private String movementType; // STOCK_IN, TRANSFER, ADJUSTMENT, ORDER_DEDUCTION

    private String reason;
    private String performedBy;

    @CreatedDate
    @Indexed
    private Instant createdAt;
}
