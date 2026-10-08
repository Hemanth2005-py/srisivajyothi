package com.mymart.controller;

import com.mymart.dto.ApiResponse;
import com.mymart.dto.ProductRequest;
import com.mymart.dto.ProductResponse;
import com.mymart.service.ProductService;
import com.mymart.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin: Products", description = "Admin CRUD management for catalog products")
public class AdminProductController {

    private final ProductService productService;
    private final UserService userService;

    @GetMapping
    @Operation(summary = "Get all products for admin catalog view")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getAllProducts() {
        return ResponseEntity.ok(ApiResponse.ok(productService.getAllAdminProducts()));
    }

    @PostMapping
    @Operation(summary = "Create a new supermarket product with initial stock")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(@Valid @RequestBody ProductRequest request) {
        String adminName = userService.getCurrentUserEntity().getFullName();
        ProductResponse created = productService.createProduct(request, adminName);
        return ResponseEntity.ok(ApiResponse.ok("Product created successfully", created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing product")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable String id,
            @Valid @RequestBody ProductRequest request) {
        String adminName = userService.getCurrentUserEntity().getFullName();
        ProductResponse updated = productService.updateProduct(id, request, adminName);
        return ResponseEntity.ok(ApiResponse.ok("Product updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate/delete a product")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable String id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.ok("Product deactivated successfully", null));
    }
}
