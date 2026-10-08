package com.mymart.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "orders")
public class Order {
    @Id
    private String id;

    @Indexed(unique = true)
    private String orderNumber;

    @Indexed
    private String userId;

    private CustomerSnapshot customerSnapshot;
    private DeliveryAddress deliveryAddress;

    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    private Double originalTotal;
    private Double discountTotal;
    private Double deliveryCharge;
    private Double taxTotal;
    private Double finalTotal;

    @Builder.Default
    private String paymentMethod = "CASH_ON_DELIVERY"; // CASH_ON_DELIVERY, ONLINE

    @Builder.Default
    private String paymentStatus = "PENDING"; // PENDING, PAID, FAILED

    @Builder.Default
    private String orderStatus = "PENDING"; // PENDING, CONFIRMED, PROCESSING, PACKED, OUT_FOR_DELIVERY, DELIVERED, CANCELLED

    @CreatedDate
    @Indexed
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerSnapshot {
        private String name;
        private String email;
        private String phone;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeliveryAddress {
        private String street;
        private String city;
        private String state;
        private String pincode;
        private String landmark;
        private String deliveryInstructions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItem {
        private String productId;
        private String productName;
        private String sku;
        private String brandName;
        private String unit;
        private String imageUrl;
        private int quantity;
        private Double originalUnitPrice;
        private Double discountUnitPrice;
        private Double subtotal;
    }
}
