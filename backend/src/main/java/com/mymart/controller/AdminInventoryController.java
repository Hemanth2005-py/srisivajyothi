package com.mymart.controller;

import com.mymart.dto.*;
import com.mymart.model.Inventory;
import com.mymart.model.StockMovement;
import com.mymart.service.InventoryService;
import com.mymart.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/inventory")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin: Inventory", description = "Stock control across independent storage locations 'Main' and 'store'")
public class AdminInventoryController {

    private final InventoryService inventoryService;
    private final UserService userService;

    @GetMapping
    @Operation(summary = "Get inventory overview across Main and store locations")
    public ResponseEntity<ApiResponse<List<ProductStockOverviewDto>>> getInventoryOverview() {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getAllInventoryOverview()));
    }

    @PostMapping("/stock-in")
    @Operation(summary = "Add received stock to location ('Main' or 'store')")
    public ResponseEntity<ApiResponse<Inventory>> stockIn(@Valid @RequestBody StockInRequest request) {
        String adminName = userService.getCurrentUserEntity().getFullName();
        Inventory updated = inventoryService.stockIn(request, adminName);
        return ResponseEntity.ok(ApiResponse.ok("Stock added successfully to " + request.getLocation(), updated));
    }

    @PostMapping("/adjustments")
    @Operation(summary = "Adjust stock quantity for location ('Main' or 'store')")
    public ResponseEntity<ApiResponse<Inventory>> adjustStock(@Valid @RequestBody StockAdjustmentRequest request) {
        String adminName = userService.getCurrentUserEntity().getFullName();
        Inventory updated = inventoryService.adjustStock(request, adminName);
        return ResponseEntity.ok(ApiResponse.ok("Stock adjusted successfully in " + request.getLocation(), updated));
    }

    @PostMapping("/transfers")
    @Operation(summary = "Transfer stock between storage locations ('Main' and 'store')")
    public ResponseEntity<ApiResponse<Void>> transferStock(@Valid @RequestBody StockTransferRequest request) {
        String adminName = userService.getCurrentUserEntity().getFullName();
        inventoryService.transferStock(request, adminName);
        return ResponseEntity.ok(ApiResponse.ok("Stock transferred successfully from " +
                request.getFromLocation() + " to " + request.getToLocation(), null));
    }

    @GetMapping("/movements")
    @Operation(summary = "Get historical audit log of stock movements")
    public ResponseEntity<ApiResponse<Page<StockMovement>>> getStockMovements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getStockMovements(PageRequest.of(page, size))));
    }

    @GetMapping("/movements/{productId}")
    @Operation(summary = "Get stock movement history for a specific product")
    public ResponseEntity<ApiResponse<List<StockMovement>>> getProductStockMovements(@PathVariable String productId) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getProductStockMovements(productId)));
    }
}
