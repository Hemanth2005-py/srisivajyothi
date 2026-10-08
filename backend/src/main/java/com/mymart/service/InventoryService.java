package com.mymart.service;

import com.mymart.dto.*;
import com.mymart.exception.BadRequestException;
import com.mymart.exception.InsufficientStockException;
import com.mymart.exception.ResourceNotFoundException;
import com.mymart.model.Category;
import com.mymart.model.Inventory;
import com.mymart.model.Product;
import com.mymart.model.StockMovement;
import com.mymart.repository.CategoryRepository;
import com.mymart.repository.InventoryRepository;
import com.mymart.repository.ProductRepository;
import com.mymart.repository.StockMovementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public void initInventoryForProduct(String productId, String sku, int mainQty, int storeQty) {
        getOrCreateInventory(productId, sku, Inventory.LOCATION_MAIN, mainQty);
        getOrCreateInventory(productId, sku, Inventory.LOCATION_STORE, storeQty);
    }

    public Inventory getOrCreateInventory(String productId, String sku, String location, int initialQty) {
        validateLocation(location);
        return inventoryRepository.findByProductIdAndLocation(productId, location)
                .orElseGet(() -> {
                    Inventory inv = Inventory.builder()
                            .productId(productId)
                            .sku(sku)
                            .location(location)
                            .quantity(Math.max(0, initialQty))
                            .lowStockThreshold(10)
                            .updatedAt(Instant.now())
                            .build();
                    return inventoryRepository.save(inv);
                });
    }

    public int getMainQuantity(String productId) {
        return inventoryRepository.findByProductIdAndLocation(productId, Inventory.LOCATION_MAIN)
                .map(Inventory::getQuantity)
                .orElse(0);
    }

    public int getStoreQuantity(String productId) {
        return inventoryRepository.findByProductIdAndLocation(productId, Inventory.LOCATION_STORE)
                .map(Inventory::getQuantity)
                .orElse(0);
    }

    public int getTotalQuantity(String productId) {
        return getMainQuantity(productId) + getStoreQuantity(productId);
    }

    @Transactional
    public synchronized Inventory stockIn(StockInRequest request, String adminName) {
        validateLocation(request.getLocation());
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));

        Inventory inv = getOrCreateInventory(product.getId(), product.getSku(), request.getLocation(), 0);
        int oldQty = inv.getQuantity();
        int newQty = oldQty + request.getQuantity();
        inv.setQuantity(newQty);
        inv.setUpdatedAt(Instant.now());
        Inventory saved = inventoryRepository.save(inv);

        // Record movement
        StockMovement movement = StockMovement.builder()
                .productId(product.getId())
                .productName(product.getName())
                .sku(product.getSku())
                .sourceLocation(null)
                .destinationLocation(request.getLocation())
                .quantity(request.getQuantity())
                .movementType("STOCK_IN")
                .reason(request.getReason() != null ? request.getReason() : "Stock received into " + request.getLocation())
                .performedBy(adminName != null ? adminName : "Admin")
                .createdAt(Instant.now())
                .build();
        stockMovementRepository.save(movement);

        log.info("Stock in: Product {}, Location {}, Added {}, New Total {}",
                product.getName(), request.getLocation(), request.getQuantity(), newQty);
        return saved;
    }

    @Transactional
    public synchronized Inventory adjustStock(StockAdjustmentRequest request, String adminName) {
        validateLocation(request.getLocation());
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));

        Inventory inv = getOrCreateInventory(product.getId(), product.getSku(), request.getLocation(), 0);
        int oldQty = inv.getQuantity();
        int diff = request.getNewQuantity() - oldQty;
        inv.setQuantity(request.getNewQuantity());
        inv.setUpdatedAt(Instant.now());
        Inventory saved = inventoryRepository.save(inv);

        // Record movement
        StockMovement movement = StockMovement.builder()
                .productId(product.getId())
                .productName(product.getName())
                .sku(product.getSku())
                .sourceLocation(diff < 0 ? request.getLocation() : null)
                .destinationLocation(diff > 0 ? request.getLocation() : null)
                .quantity(Math.abs(diff))
                .movementType("ADJUSTMENT")
                .reason(request.getReason() != null ? request.getReason() : "Stock adjusted from " + oldQty + " to " + request.getNewQuantity())
                .performedBy(adminName != null ? adminName : "Admin")
                .createdAt(Instant.now())
                .build();
        stockMovementRepository.save(movement);

        log.info("Stock adjusted: Product {}, Location {}, From {} to {}",
                product.getName(), request.getLocation(), oldQty, request.getNewQuantity());
        return saved;
    }

    @Transactional
    public synchronized void transferStock(StockTransferRequest request, String adminName) {
        validateLocation(request.getFromLocation());
        validateLocation(request.getToLocation());

        if (request.getFromLocation().equals(request.getToLocation())) {
            throw new BadRequestException("Source and destination storage locations cannot be the same");
        }

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));

        Inventory sourceInv = getOrCreateInventory(product.getId(), product.getSku(), request.getFromLocation(), 0);
        if (sourceInv.getQuantity() < request.getQuantity()) {
            throw new InsufficientStockException("Insufficient stock in " + request.getFromLocation() +
                    ". Available: " + sourceInv.getQuantity() + ", requested: " + request.getQuantity());
        }

        Inventory destInv = getOrCreateInventory(product.getId(), product.getSku(), request.getToLocation(), 0);

        sourceInv.setQuantity(sourceInv.getQuantity() - request.getQuantity());
        sourceInv.setUpdatedAt(Instant.now());
        inventoryRepository.save(sourceInv);

        destInv.setQuantity(destInv.getQuantity() + request.getQuantity());
        destInv.setUpdatedAt(Instant.now());
        inventoryRepository.save(destInv);

        StockMovement movement = StockMovement.builder()
                .productId(product.getId())
                .productName(product.getName())
                .sku(product.getSku())
                .sourceLocation(request.getFromLocation())
                .destinationLocation(request.getToLocation())
                .quantity(request.getQuantity())
                .movementType("TRANSFER")
                .reason(request.getReason() != null ? request.getReason() : "Transferred from " + request.getFromLocation() + " to " + request.getToLocation())
                .performedBy(adminName != null ? adminName : "Admin")
                .createdAt(Instant.now())
                .build();
        stockMovementRepository.save(movement);

        log.info("Stock transferred: Product {}, {} units from {} to {}",
                product.getName(), request.getQuantity(), request.getFromLocation(), request.getToLocation());
    }

    @Transactional
    public synchronized void deductStockForOrder(String productId, int quantity, String orderNumber) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        Inventory mainInv = getOrCreateInventory(productId, product.getSku(), Inventory.LOCATION_MAIN, 0);
        Inventory storeInv = getOrCreateInventory(productId, product.getSku(), Inventory.LOCATION_STORE, 0);

        int totalAvailable = mainInv.getQuantity() + storeInv.getQuantity();
        if (totalAvailable < quantity) {
            throw new InsufficientStockException("Insufficient inventory for product '" + product.getName() +
                    "'. Required: " + quantity + ", available: " + totalAvailable);
        }

        // Fulfillment Strategy: Deduct from Main first (primary fulfillment warehouse),
        // and if remainder needed, deduct from store.
        int remainingToDeduct = quantity;
        if (mainInv.getQuantity() > 0) {
            int deductFromMain = Math.min(mainInv.getQuantity(), remainingToDeduct);
            mainInv.setQuantity(mainInv.getQuantity() - deductFromMain);
            mainInv.setUpdatedAt(Instant.now());
            inventoryRepository.save(mainInv);
            remainingToDeduct -= deductFromMain;

            StockMovement m = StockMovement.builder()
                    .productId(productId)
                    .productName(product.getName())
                    .sku(product.getSku())
                    .sourceLocation(Inventory.LOCATION_MAIN)
                    .destinationLocation(null)
                    .quantity(deductFromMain)
                    .movementType("ORDER_DEDUCTION")
                    .reason("Order fulfilled: " + orderNumber)
                    .performedBy("System")
                    .createdAt(Instant.now())
                    .build();
            stockMovementRepository.save(m);
        }

        if (remainingToDeduct > 0) {
            storeInv.setQuantity(storeInv.getQuantity() - remainingToDeduct);
            storeInv.setUpdatedAt(Instant.now());
            inventoryRepository.save(storeInv);

            StockMovement m = StockMovement.builder()
                    .productId(productId)
                    .productName(product.getName())
                    .sku(product.getSku())
                    .sourceLocation(Inventory.LOCATION_STORE)
                    .destinationLocation(null)
                    .quantity(remainingToDeduct)
                    .movementType("ORDER_DEDUCTION")
                    .reason("Order fulfilled from store stock: " + orderNumber)
                    .performedBy("System")
                    .createdAt(Instant.now())
                    .build();
            stockMovementRepository.save(m);
        }
    }

    public List<ProductStockOverviewDto> getAllInventoryOverview() {
        List<Product> products = productRepository.findAll();
        Map<String, String> categoryNames = categoryRepository.findAll().stream()
                .collect(Collectors.toMap(Category::getId, Category::getName, (a, b) -> a));

        List<ProductStockOverviewDto> list = new ArrayList<>();
        for (Product product : products) {
            int mainQ = getMainQuantity(product.getId());
            int storeQ = getStoreQuantity(product.getId());
            int totalQ = mainQ + storeQ;
            int threshold = 10;

            list.add(ProductStockOverviewDto.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .sku(product.getSku())
                    .categoryName(categoryNames.getOrDefault(product.getCategoryId(), "General"))
                    .mainQuantity(mainQ)
                    .storeQuantity(storeQ)
                    .totalQuantity(totalQ)
                    .lowStockThreshold(threshold)
                    .lowStock(totalQ > 0 && totalQ <= threshold)
                    .outOfStock(totalQ <= 0)
                    .build());
        }
        return list;
    }

    public Page<StockMovement> getStockMovements(Pageable pageable) {
        return stockMovementRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    public List<StockMovement> getProductStockMovements(String productId) {
        return stockMovementRepository.findByProductIdOrderByCreatedAtDesc(productId);
    }

    public void validateLocation(String location) {
        if (!Inventory.isValidLocation(location)) {
            throw new BadRequestException("Invalid storage location '" + location + "'. Must be exactly 'Main' or 'store'.");
        }
    }
}
