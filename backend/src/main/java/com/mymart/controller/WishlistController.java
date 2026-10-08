package com.mymart.controller;

import com.mymart.dto.ApiResponse;
import com.mymart.dto.WishlistResponse;
import com.mymart.service.UserService;
import com.mymart.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
@Tag(name = "Wishlist / Favorites", description = "Endpoints for managing customer favorite products")
public class WishlistController {

    private final WishlistService wishlistService;
    private final UserService userService;

    @GetMapping
    @Operation(summary = "Get all favorite products for current user")
    public ResponseEntity<ApiResponse<WishlistResponse>> getWishlist() {
        String userId = userService.getCurrentUserEntity().getId();
        return ResponseEntity.ok(ApiResponse.ok(wishlistService.getWishlist(userId)));
    }

    @PostMapping("/{productId}")
    @Operation(summary = "Add a product to favorites")
    public ResponseEntity<ApiResponse<WishlistResponse>> addToWishlist(@PathVariable String productId) {
        String userId = userService.getCurrentUserEntity().getId();
        return ResponseEntity.ok(ApiResponse.ok("Added to favorites", wishlistService.addToWishlist(userId, productId)));
    }

    @DeleteMapping("/{productId}")
    @Operation(summary = "Remove a product from favorites")
    public ResponseEntity<ApiResponse<WishlistResponse>> removeFromWishlist(@PathVariable String productId) {
        String userId = userService.getCurrentUserEntity().getId();
        return ResponseEntity.ok(ApiResponse.ok("Removed from favorites", wishlistService.removeFromWishlist(userId, productId)));
    }

    @PostMapping("/{productId}/toggle")
    @Operation(summary = "Toggle favorite status for a product")
    public ResponseEntity<ApiResponse<Map<String, Object>>> toggleWishlist(@PathVariable String productId) {
        String userId = userService.getCurrentUserEntity().getId();
        boolean added = wishlistService.toggleWishlist(userId, productId);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("productId", productId, "favorited", added)));
    }

    @PostMapping("/sync")
    @Operation(summary = "Sync guest wishlist with user account upon login")
    public ResponseEntity<ApiResponse<WishlistResponse>> syncGuestWishlist(@RequestBody List<String> productIds) {
        String userId = userService.getCurrentUserEntity().getId();
        return ResponseEntity.ok(ApiResponse.ok("Wishlist synchronized", wishlistService.syncGuestWishlist(userId, productIds)));
    }
}
