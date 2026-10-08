package com.mymart.dto;

import com.mymart.model.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private String id;
    private String orderNumber;
    private String userId;
    private Order.CustomerSnapshot customerSnapshot;
    private Order.DeliveryAddress deliveryAddress;
    private List<Order.OrderItem> items;
    private Double originalTotal;
    private Double discountTotal;
    private Double deliveryCharge;
    private Double taxTotal;
    private Double finalTotal;
    private String paymentMethod;
    private String paymentStatus;
    private String orderStatus;
    private Instant createdAt;
    private Instant updatedAt;
}
