package com.mymart.controller;

import com.mymart.dto.ApiResponse;
import com.mymart.dto.PagedResponse;
import com.mymart.dto.ProductFilterCriteria;
import com.mymart.dto.ProductResponse;
import com.mymart.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products (Customer)", description = "Public endpoints for catalog browsing, live search, and filtering")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Get products with pagination, search, and multi-filters")
    public ResponseEntity<ApiResponse<PagedResponse<ProductResponse>>> getProducts(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String brandId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Boolean discountOnly,
            @RequestParam(required = false) Boolean inStockOnly,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        ProductFilterCriteria criteria = ProductFilterCriteria.builder()
                .query(query)
                .categoryId(categoryId)
                .brandId(brandId)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .discountOnly(discountOnly)
                .inStockOnly(inStockOnly)
                .sort(sort)
                .build();

        PagedResponse<ProductResponse> response = productService.getProducts(criteria, page, size);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product details by ID")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(productService.getProductById(id)));
    }

    @GetMapping("/featured")
    @Operation(summary = "Get featured supermarket products")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getFeatured() {
        return ResponseEntity.ok(ApiResponse.ok(productService.getFeaturedProducts()));
    }

    @GetMapping("/bestsellers")
    @Operation(summary = "Get best-selling supermarket products")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getBestsellers() {
        return ResponseEntity.ok(ApiResponse.ok(productService.getBestsellerProducts()));
    }

    @GetMapping("/{id}/related")
    @Operation(summary = "Get related products within the same category")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getRelatedProducts(
            @PathVariable String id,
            @RequestParam(defaultValue = "4") int limit) {
        return ResponseEntity.ok(ApiResponse.ok(productService.getRelatedProducts(id, limit)));
    }
}
