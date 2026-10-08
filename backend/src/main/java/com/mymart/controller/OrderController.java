package com.mymart.controller;

import com.mymart.dto.ApiResponse;
import com.mymart.dto.CreateOrderRequest;
import com.mymart.dto.OrderResponse;
import com.mymart.dto.PagedResponse;
import com.mymart.model.Order;
import com.mymart.service.InvoicePdfService;
import com.mymart.service.OrderService;
import com.mymart.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders (Customer)", description = "Endpoints for customer order placement and history")
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;
    private final InvoicePdfService invoicePdfService;

    @PostMapping
    @Operation(summary = "Place a new customer order")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        String userId = userService.getCurrentUserEntity().getId();
        OrderResponse response = orderService.createOrder(userId, request);
        return ResponseEntity.ok(ApiResponse.ok("Order placed successfully! Order #" + response.getOrderNumber(), response));
    }

    @GetMapping("/my")
    @Operation(summary = "Get order history for authenticated customer")
    public ResponseEntity<ApiResponse<PagedResponse<OrderResponse>>> getMyOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        String userId = userService.getCurrentUserEntity().getId();
        return ResponseEntity.ok(ApiResponse.ok(orderService.getMyOrders(userId, page, size)));
    }

    @GetMapping("/{orderNumber}")
    @Operation(summary = "Get details of an order by order number")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderByNumber(@PathVariable String orderNumber) {
        String userId = userService.getCurrentUserEntity().getId();
        return ResponseEntity.ok(ApiResponse.ok(orderService.getOrderByNumber(orderNumber, userId, false)));
    }

    @GetMapping("/{orderNumber}/bill")
    @Operation(summary = "Download printable PDF bill/invoice for customer order")
    public ResponseEntity<byte[]> downloadOrderBill(@PathVariable String orderNumber) {
        String userId = userService.getCurrentUserEntity().getId();
        Order order = orderService.getOrderEntityByNumber(orderNumber);

        // Security check
        if (!order.getUserId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }

        byte[] pdfBytes = invoicePdfService.generateOrderInvoice(order);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + orderNumber + "-invoice.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
