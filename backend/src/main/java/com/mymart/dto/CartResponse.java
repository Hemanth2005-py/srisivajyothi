package com.mymart.dto;

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
public class CartResponse {
    @Builder.Default
    private List<CartItemResponse> items = new ArrayList<>();
    private int totalItemCount;
    private Double originalTotal;
    private Double discountTotal;
    private Double merchandiseSubtotal;
    private Double deliveryCharge;
    private Double taxTotal;
    private Double finalPayableAmount;
    private Double freeDeliveryThreshold;
    private boolean freeDeliveryEligible;
    private Double amountNeededForFreeDelivery;
}
