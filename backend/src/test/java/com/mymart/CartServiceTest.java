package com.mymart;

import com.mymart.dto.AddToCartRequest;
import com.mymart.dto.CartResponse;
import com.mymart.exception.InsufficientStockException;
import com.mymart.model.Brand;
import com.mymart.model.Cart;
import com.mymart.model.Product;
import com.mymart.repository.BrandRepository;
import com.mymart.repository.CartRepository;
import com.mymart.repository.ProductRepository;
import com.mymart.service.CartService;
import com.mymart.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private BrandRepository brandRepository;

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private CartService cartService;

    private Product testProduct;
    private Cart testCart;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(cartService, "freeDeliveryThreshold", 299.0);
        ReflectionTestUtils.setField(cartService, "standardDeliveryFee", 35.0);
        ReflectionTestUtils.setField(cartService, "taxRate", 0.05);

        testProduct = Product.builder()
                .id("prod-1")
                .name("Amul Butter")
                .sku("SKU-AML-01")
                .brandId("brand-1")
                .originalPrice(100.0)
                .discountPrice(80.0) // 20% discount
                .active(true)
                .build();

        testCart = Cart.builder()
                .id("cart-1")
                .userId("user-1")
                .items(new ArrayList<>())
                .build();
    }

    @Test
    void testAddToCart_Success() {
        when(productRepository.findById("prod-1")).thenReturn(Optional.of(testProduct));
        when(inventoryService.getTotalQuantity("prod-1")).thenReturn(15);
        when(cartRepository.findByUserId("user-1")).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        when(brandRepository.findById("brand-1")).thenReturn(Optional.of(Brand.builder().name("Amul").build()));

        AddToCartRequest req = new AddToCartRequest("prod-1", 2);
        CartResponse res = cartService.addToCart("user-1", req);

        assertNotNull(res);
        assertEquals(1, res.getItems().size());
        assertEquals(2, res.getTotalItemCount());
        assertEquals(200.0, res.getOriginalTotal());
        assertEquals(160.0, res.getMerchandiseSubtotal());
        assertEquals(40.0, res.getDiscountTotal());
        // Since 160 < 299, delivery fee 35 applies
        assertEquals(35.0, res.getDeliveryCharge());
    }

    @Test
    void testAddToCart_ExceedsStock_ThrowsException() {
        when(productRepository.findById("prod-1")).thenReturn(Optional.of(testProduct));
        when(inventoryService.getTotalQuantity("prod-1")).thenReturn(3);
        when(cartRepository.findByUserId("user-1")).thenReturn(Optional.of(testCart));

        AddToCartRequest req = new AddToCartRequest("prod-1", 5);
        assertThrows(InsufficientStockException.class, () -> cartService.addToCart("user-1", req));
    }
}
