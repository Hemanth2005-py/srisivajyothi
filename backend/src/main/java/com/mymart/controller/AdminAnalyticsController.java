package com.mymart.controller;

import com.mymart.dto.*;
import com.mymart.service.AnalyticsService;
import com.mymart.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin: Analytics", description = "Dashboard KPI summaries and real aggregation sales charts")
public class AdminAnalyticsController {

    private final AnalyticsService analyticsService;
    private final UserService userService;

    @GetMapping("/overview")
    @Operation(summary = "Get overall dashboard business KPIs from MongoDB")
    public ResponseEntity<ApiResponse<DashboardOverviewDto>> getOverview() {
        return ResponseEntity.ok(ApiResponse.ok(analyticsService.getDashboardOverview()));
    }

    @GetMapping("/sales")
    @Operation(summary = "Get sales time-series chart data (today, 7days, 30days, 3months, 6months)")
    public ResponseEntity<ApiResponse<SalesChartDto>> getSalesChart(
            @RequestParam(defaultValue = "30days") String range) {
        return ResponseEntity.ok(ApiResponse.ok(analyticsService.getSalesChart(range)));
    }

    @GetMapping("/categories")
    @Operation(summary = "Get revenue distribution by category")
    public ResponseEntity<ApiResponse<List<CategorySalesDto>>> getCategorySales() {
        return ResponseEntity.ok(ApiResponse.ok(analyticsService.getCategorySales()));
    }

    @GetMapping("/brands")
    @Operation(summary = "Get revenue distribution by brand")
    public ResponseEntity<ApiResponse<List<BrandSalesDto>>> getBrandSales() {
        return ResponseEntity.ok(ApiResponse.ok(analyticsService.getBrandSales()));
    }

    @GetMapping("/customers")
    @Operation(summary = "Get registered customers list")
    public ResponseEntity<ApiResponse<List<UserDto>>> getCustomers() {
        return ResponseEntity.ok(ApiResponse.ok(userService.getAllCustomers()));
    }
}
