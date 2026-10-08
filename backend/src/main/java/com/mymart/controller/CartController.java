package com.mymart.controller;

import com.mymart.dto.*;
import com.mymart.service.CartService;
import com.mymart.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Shopping Cart", description = "Endpoints for managing the customer shopping cart")
public class CartController {

    private final CartService cartService;
    private final UserService userService;

    @GetMapping
    @Operation(summary = "Get user shopping cart with live backend calculated totals and delivery fee")
    public ResponseEntity<ApiResponse<CartResponse>> getCart() {
        String userId = userService.getCurrentUserEntity().getId();
        return ResponseEntity.ok(ApiResponse.ok(cartService.getCart(userId)));
    }

    @PostMapping("/items")
    @Operation(summary = "Add an item to the shopping cart")
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(@Valid @RequestBody AddToCartRequest request) {
        String userId = userService.getCurrentUserEntity().getId();
        return ResponseEntity.ok(ApiResponse.ok("Item added to cart", cartService.addToCart(userId, request)));
    }

    @PatchMapping("/items/{productId}")
    @Operation(summary = "Update quantity of an item in the cart")
    public ResponseEntity<ApiResponse<CartResponse>> updateCartItem(
            @PathVariable String productId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        String userId = userService.getCurrentUserEntity().getId();
        return ResponseEntity.ok(ApiResponse.ok("Cart item updated", cartService.updateCartItem(userId, productId, request.getQuantity())));
    }

    @DeleteMapping("/items/{productId}")
    @Operation(summary = "Remove an item from the cart")
    public ResponseEntity<ApiResponse<CartResponse>> removeFromCart(@PathVariable String productId) {
        String userId = userService.getCurrentUserEntity().getId();
        return ResponseEntity.ok(ApiResponse.ok("Item removed from cart", cartService.removeFromCart(userId, productId)));
    }

    @DeleteMapping
    @Operation(summary = "Clear all items from the cart")
    public ResponseEntity<ApiResponse<Void>> clearCart() {
        String userId = userService.getCurrentUserEntity().getId();
        cartService.clearCart(userId);
        return ResponseEntity.ok(ApiResponse.ok("Cart cleared", null));
    }

    @PostMapping("/sync")
    @Operation(summary = "Sync guest cart items with customer account upon login")
    public ResponseEntity<ApiResponse<CartResponse>> syncGuestCart(@RequestBody List<CreateOrderRequest.OrderItemDto> items) {
        String userId = userService.getCurrentUserEntity().getId();
        return ResponseEntity.ok(ApiResponse.ok("Cart synchronized", cartService.syncGuestCart(userId, items)));
    }
}
