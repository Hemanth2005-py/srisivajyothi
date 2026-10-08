package com.mymart.controller;

import com.mymart.dto.ApiResponse;
import com.mymart.dto.BrandRequest;
import com.mymart.model.Brand;
import com.mymart.service.BrandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/brands")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin: Brands", description = "Admin CRUD management for brands")
public class AdminBrandController {

    private final BrandService brandService;

    @GetMapping
    @Operation(summary = "Get all brands for admin table")
    public ResponseEntity<ApiResponse<List<Brand>>> getAllBrands() {
        return ResponseEntity.ok(ApiResponse.ok(brandService.getAllBrands()));
    }

    @PostMapping
    @Operation(summary = "Create a new brand")
    public ResponseEntity<ApiResponse<Brand>> createBrand(@Valid @RequestBody BrandRequest request) {
        Brand created = brandService.createBrand(request);
        return ResponseEntity.ok(ApiResponse.ok("Brand created successfully", created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing brand")
    public ResponseEntity<ApiResponse<Brand>> updateBrand(
            @PathVariable String id,
            @Valid @RequestBody BrandRequest request) {
        Brand updated = brandService.updateBrand(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Brand updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete or deactivate brand")
    public ResponseEntity<ApiResponse<Void>> deleteBrand(@PathVariable String id) {
        brandService.deleteBrand(id);
        return ResponseEntity.ok(ApiResponse.ok("Brand deleted or deactivated", null));
    }
}
