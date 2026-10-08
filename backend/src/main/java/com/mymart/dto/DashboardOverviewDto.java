package com.mymart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardOverviewDto {
    private Double totalSales;
    private long totalOrders;
    private long totalProducts;
    private long totalActiveCustomers;
    private long pendingOrders;
    private long completedOrders;
    private long lowStockProducts;
    private long outOfStockProducts;
    private Double salesGrowthPercentage;
    private Double averageOrderValue;
    private List<OrderResponse> recentOrders;
}
