package com.mymart.controller;

import com.mymart.dto.ApiResponse;
import com.mymart.model.Brand;
import com.mymart.service.BrandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
@Tag(name = "Brands (Customer)", description = "Public endpoints for brands")
public class BrandController {

    private final BrandService brandService;

    @GetMapping
    @Operation(summary = "Get all active brands")
    public ResponseEntity<ApiResponse<List<Brand>>> getBrands() {
        return ResponseEntity.ok(ApiResponse.ok(brandService.getAllActiveBrands()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get brand details by ID")
    public ResponseEntity<ApiResponse<Brand>> getBrandById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(brandService.getBrandById(id)));
    }
}
