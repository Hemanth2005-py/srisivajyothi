package com.mymart.service;

import com.mymart.dto.PagedResponse;
import com.mymart.dto.ProductFilterCriteria;
import com.mymart.dto.ProductRequest;
import com.mymart.dto.ProductResponse;
import com.mymart.exception.BadRequestException;
import com.mymart.exception.ResourceNotFoundException;
import com.mymart.model.Brand;
import com.mymart.model.Category;
import com.mymart.model.Product;
import com.mymart.repository.BrandRepository;
import com.mymart.repository.CategoryRepository;
import com.mymart.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final InventoryService inventoryService;
    private final MongoTemplate mongoTemplate;

    public PagedResponse<ProductResponse> getProducts(ProductFilterCriteria criteria, int page, int size) {
        Query query = new Query();
        query.addCriteria(Criteria.where("active").is(true));

        if (criteria != null) {
            if (criteria.getQuery() != null && !criteria.getQuery().trim().isEmpty()) {
                String searchRegex = ".*" + criteria.getQuery().trim() + ".*";
                query.addCriteria(new Criteria().orOperator(
                        Criteria.where("name").regex(searchRegex, "i"),
                        Criteria.where("description").regex(searchRegex, "i")
                ));
            }

            if (criteria.getCategoryId() != null && !criteria.getCategoryId().trim().isEmpty()) {
                query.addCriteria(Criteria.where("categoryId").is(criteria.getCategoryId()));
            }

            if (criteria.getBrandId() != null && !criteria.getBrandId().trim().isEmpty()) {
                query.addCriteria(Criteria.where("brandId").is(criteria.getBrandId()));
            }

            if (criteria.getMinPrice() != null && criteria.getMaxPrice() != null) {
                query.addCriteria(Criteria.where("originalPrice").gte(criteria.getMinPrice()).lte(criteria.getMaxPrice()));
            } else if (criteria.getMinPrice() != null) {
                query.addCriteria(Criteria.where("originalPrice").gte(criteria.getMinPrice()));
            } else if (criteria.getMaxPrice() != null) {
                query.addCriteria(Criteria.where("originalPrice").lte(criteria.getMaxPrice()));
            }

            if (Boolean.TRUE.equals(criteria.getDiscountOnly())) {
                query.addCriteria(Criteria.where("discountPrice").gt(0));
            }
        }

        long totalElements = mongoTemplate.count(query, Product.class);

        // Sorting
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        if (criteria != null && criteria.getSort() != null) {
            switch (criteria.getSort().toLowerCase()) {
                case "price_asc" -> sort = Sort.by(Sort.Direction.ASC, "originalPrice");
                case "price_desc" -> sort = Sort.by(Sort.Direction.DESC, "originalPrice");
                case "newest" -> sort = Sort.by(Sort.Direction.DESC, "createdAt");
                case "popular" -> sort = Sort.by(Sort.Direction.DESC, "bestseller", "createdAt");
                default -> {}
            }
        }

        Pageable pageable = PageRequest.of(page, size, sort);
        query.with(pageable);

        List<Product> products = mongoTemplate.find(query, Product.class);
        List<ProductResponse> responses = products.stream()
                .map(this::toResponse)
                .filter(res -> {
                    if (criteria != null && Boolean.TRUE.equals(criteria.getInStockOnly())) {
                        return res.isInStock();
                    }
                    return true;
                })
                .collect(Collectors.toList());

        int totalPages = (int) Math.ceil((double) totalElements / size);

        return PagedResponse.<ProductResponse>builder()
                .content(responses)
                .pageNumber(page)
                .pageSize(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .last(page >= totalPages - 1)
                .build();
    }

    public ProductResponse getProductById(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return toResponse(product);
    }

    public List<ProductResponse> getFeaturedProducts() {
        return productRepository.findByActiveTrueAndFeaturedTrue().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<ProductResponse> getBestsellerProducts() {
        return productRepository.findByActiveTrueAndBestsellerTrue().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<ProductResponse> getRelatedProducts(String productId, int limit) {
        Product current = productRepository.findById(productId).orElse(null);
        if (current == null) return List.of();

        List<Product> sameCategory = productRepository.findByCategoryId(current.getCategoryId()).stream()
                .filter(p -> p.isActive() && !p.getId().equals(productId))
                .limit(limit)
                .toList();

        return sameCategory.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<ProductResponse> getAllAdminProducts() {
        return productRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ProductResponse createProduct(ProductRequest request, String adminName) {
        validateProductRequest(request);

        if (productRepository.existsBySku(request.getSku().trim())) {
            throw new BadRequestException("Product already exists with SKU: " + request.getSku());
        }

        List<String> images = request.getImageUrls() != null ? new ArrayList<>(request.getImageUrls()) : new ArrayList<>();
        if (request.getImageUrl() != null && !images.contains(request.getImageUrl())) {
            images.add(0, request.getImageUrl());
        }

        Product product = Product.builder()
                .name(request.getName().trim())
                .brandId(request.getBrandId())
                .categoryId(request.getCategoryId())
                .description(request.getDescription())
                .weight(request.getWeight())
                .unit(request.getUnit().trim())
                .originalPrice(request.getOriginalPrice())
                .discountPrice(request.getDiscountPrice())
                .sku(request.getSku().trim().toUpperCase())
                .imageUrl(request.getImageUrl() != null ? request.getImageUrl() : (images.isEmpty() ? null : images.get(0)))
                .imageUrls(images)
                .active(request.isActive())
                .featured(request.isFeatured())
                .bestseller(request.isBestseller())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        Product saved = productRepository.save(product);

        // Initialize inventory for Main and store
        int initialMain = request.getInitialMainStock() != null ? request.getInitialMainStock() : 25;
        int initialStore = request.getInitialStoreStock() != null ? request.getInitialStoreStock() : 10;
        inventoryService.initInventoryForProduct(saved.getId(), saved.getSku(), initialMain, initialStore);

        return toResponse(saved);
    }

    public ProductResponse updateProduct(String id, ProductRequest request, String adminName) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        validateProductRequest(request);

        if (!product.getSku().equalsIgnoreCase(request.getSku().trim()) &&
                productRepository.existsBySku(request.getSku().trim())) {
            throw new BadRequestException("Product already exists with SKU: " + request.getSku());
        }

        List<String> images = request.getImageUrls() != null ? new ArrayList<>(request.getImageUrls()) : new ArrayList<>();
        if (request.getImageUrl() != null && !images.contains(request.getImageUrl())) {
            images.add(0, request.getImageUrl());
        }

        product.setName(request.getName().trim());
        product.setBrandId(request.getBrandId());
        product.setCategoryId(request.getCategoryId());
        product.setDescription(request.getDescription());
        product.setWeight(request.getWeight());
        product.setUnit(request.getUnit().trim());
        product.setOriginalPrice(request.getOriginalPrice());
        product.setDiscountPrice(request.getDiscountPrice());
        product.setSku(request.getSku().trim().toUpperCase());
        product.setImageUrl(request.getImageUrl() != null ? request.getImageUrl() : (images.isEmpty() ? null : images.get(0)));
        product.setImageUrls(images);
        product.setActive(request.isActive());
        product.setFeatured(request.isFeatured());
        product.setBestseller(request.isBestseller());
        product.setUpdatedAt(Instant.now());

        Product updated = productRepository.save(product);
        return toResponse(updated);
    }

    public void deleteProduct(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        // Soft-delete to preserve past order line item history as required
        product.setActive(false);
        productRepository.save(product);
        log.info("Soft-deleted (deactivated) product {}", id);
    }

    private void validateProductRequest(ProductRequest request) {
        if (request.getDiscountPrice() != null && request.getDiscountPrice() > 0) {
            if (request.getDiscountPrice() > request.getOriginalPrice()) {
                throw new BadRequestException("Discount price (₹" + request.getDiscountPrice() +
                        ") cannot be greater than original price (₹" + request.getOriginalPrice() + ")");
            }
        }

        if (!categoryRepository.existsById(request.getCategoryId())) {
            throw new BadRequestException("Selected category does not exist: " + request.getCategoryId());
        }

        if (!brandRepository.existsById(request.getBrandId())) {
            throw new BadRequestException("Selected brand does not exist: " + request.getBrandId());
        }
    }

    public ProductResponse toResponse(Product product) {
        String categoryName = categoryRepository.findById(product.getCategoryId())
                .map(Category::getName)
                .orElse("General");

        String brandName = brandRepository.findById(product.getBrandId())
                .map(Brand::getName)
                .orElse("Generic");

        int stockMain = inventoryService.getMainQuantity(product.getId());
        int stockStore = inventoryService.getStoreQuantity(product.getId());
        int totalStock = stockMain + stockStore;

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .brandId(product.getBrandId())
                .brandName(brandName)
                .categoryId(product.getCategoryId())
                .categoryName(categoryName)
                .description(product.getDescription())
                .weight(product.getWeight())
                .unit(product.getUnit())
                .originalPrice(product.getOriginalPrice())
                .discountPrice(product.getDiscountPrice())
                .effectivePrice(product.getEffectivePrice())
                .discountPercentage(product.getDiscountPercentage())
                .sku(product.getSku())
                .imageUrl(product.getImageUrl())
                .imageUrls(product.getImageUrls())
                .active(product.isActive())
                .featured(product.isFeatured())
                .bestseller(product.isBestseller())
                .stockMain(stockMain)
                .stockStore(stockStore)
                .totalStock(totalStock)
                .inStock(totalStock > 0)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
