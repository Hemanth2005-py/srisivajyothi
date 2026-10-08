package com.mymart.controller;

import com.mymart.dto.ApiResponse;
import com.mymart.dto.OrderResponse;
import com.mymart.dto.OrderStatusUpdateDto;
import com.mymart.dto.PagedResponse;
import com.mymart.model.Order;
import com.mymart.service.InvoicePdfService;
import com.mymart.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin: Orders", description = "Admin order management and status transitions")
public class AdminOrderController {

    private final OrderService orderService;
    private final InvoicePdfService invoicePdfService;

    @GetMapping
    @Operation(summary = "Get all customer orders with filtering and pagination")
    public ResponseEntity<ApiResponse<PagedResponse<OrderResponse>>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getAllOrders(page, size, status)));
    }

    @GetMapping("/{orderNumber}")
    @Operation(summary = "Get complete details of an order")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderByNumber(@PathVariable String orderNumber) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getOrderByNumber(orderNumber, null, true)));
    }

    @PatchMapping("/{orderNumber}/status")
    @Operation(summary = "Update order status (e.g. Confirmed, Packed, Out for Delivery, Delivered)")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @PathVariable String orderNumber,
            @Valid @RequestBody OrderStatusUpdateDto dto) {
        OrderResponse updated = orderService.updateOrderStatus(orderNumber, dto);
        return ResponseEntity.ok(ApiResponse.ok("Order status updated successfully", updated));
    }

    @GetMapping("/{orderNumber}/bill")
    @Operation(summary = "Download PDF bill/tax invoice for order")
    public ResponseEntity<byte[]> downloadOrderBill(@PathVariable String orderNumber) {
        Order order = orderService.getOrderEntityByNumber(orderNumber);
        byte[] pdfBytes = invoicePdfService.generateOrderInvoice(order);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + orderNumber + "-invoice.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
