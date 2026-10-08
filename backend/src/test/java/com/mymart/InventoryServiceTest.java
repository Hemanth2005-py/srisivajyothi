package com.mymart;

import com.mymart.dto.StockInRequest;
import com.mymart.dto.StockTransferRequest;
import com.mymart.exception.BadRequestException;
import com.mymart.exception.InsufficientStockException;
import com.mymart.model.Inventory;
import com.mymart.model.Product;
import com.mymart.repository.CategoryRepository;
import com.mymart.repository.InventoryRepository;
import com.mymart.repository.ProductRepository;
import com.mymart.repository.StockMovementRepository;
import com.mymart.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private Product product;
    private Inventory mainInv;
    private Inventory storeInv;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id("prod-1")
                .name("Tata Salt")
                .sku("SKU-TAT-SLT")
                .build();

        mainInv = Inventory.builder()
                .id("inv-main")
                .productId("prod-1")
                .sku("SKU-TAT-SLT")
                .location("Main")
                .quantity(50)
                .build();

        storeInv = Inventory.builder()
                .id("inv-store")
                .productId("prod-1")
                .sku("SKU-TAT-SLT")
                .location("store")
                .quantity(20)
                .build();
    }

    @Test
    void testStockIn_ValidLocation_Success() {
        when(productRepository.findById("prod-1")).thenReturn(Optional.of(product));
        when(inventoryRepository.findByProductIdAndLocation("prod-1", "Main")).thenReturn(Optional.of(mainInv));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(i -> i.getArgument(0));

        StockInRequest request = new StockInRequest("prod-1", "Main", 25, "Restock batch");
        Inventory updated = inventoryService.stockIn(request, "Admin");

        assertNotNull(updated);
        assertEquals(75, updated.getQuantity());
        verify(stockMovementRepository, times(1)).save(any());
    }

    @Test
    void testStockIn_InvalidLocation_ThrowsBadRequest() {
        StockInRequest request = new StockInRequest("prod-1", "Warehouse2", 10, "Restock");
        assertThrows(BadRequestException.class, () -> inventoryService.stockIn(request, "Admin"));
    }

    @Test
    void testTransferStock_Success() {
        when(productRepository.findById("prod-1")).thenReturn(Optional.of(product));
        when(inventoryRepository.findByProductIdAndLocation("prod-1", "Main")).thenReturn(Optional.of(mainInv));
        when(inventoryRepository.findByProductIdAndLocation("prod-1", "store")).thenReturn(Optional.of(storeInv));

        StockTransferRequest request = new StockTransferRequest("prod-1", "Main", "store", 15, "Replenish store");
        inventoryService.transferStock(request, "Admin");

        assertEquals(35, mainInv.getQuantity());
        assertEquals(35, storeInv.getQuantity());
        verify(inventoryRepository, times(2)).save(any());
        verify(stockMovementRepository, times(1)).save(any());
    }

    @Test
    void testTransferStock_InsufficientSource_ThrowsException() {
        when(productRepository.findById("prod-1")).thenReturn(Optional.of(product));
        when(inventoryRepository.findByProductIdAndLocation("prod-1", "store")).thenReturn(Optional.of(storeInv));

        StockTransferRequest request = new StockTransferRequest("prod-1", "store", "Main", 50, "Over transfer");
        assertThrows(InsufficientStockException.class, () -> inventoryService.transferStock(request, "Admin"));
    }
}
