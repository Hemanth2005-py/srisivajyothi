package com.mymart;

import com.mymart.dto.OrderStatusUpdateDto;
import com.mymart.exception.BadRequestException;
import com.mymart.model.Order;
import com.mymart.repository.*;
import com.mymart.service.InventoryService;
import com.mymart.service.OrderService;
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
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private BrandRepository brandRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private OrderService orderService;

    private Order testOrder;

    @BeforeEach
    void setUp() {
        testOrder = Order.builder()
                .id("ord-1")
                .orderNumber("MM-20261008-1001")
                .orderStatus("PENDING")
                .paymentStatus("PENDING")
                .paymentMethod("CASH_ON_DELIVERY")
                .build();
    }

    @Test
    void testUpdateOrderStatus_ValidTransition_Success() {
        when(orderRepository.findByOrderNumber("MM-20261008-1001")).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        OrderStatusUpdateDto dto = new OrderStatusUpdateDto("CONFIRMED", "Confirmed by warehouse");
        var res = orderService.updateOrderStatus("MM-20261008-1001", dto);

        assertEquals("CONFIRMED", res.getOrderStatus());
    }

    @Test
    void testUpdateOrderStatus_DeliveredCannotBeCancelled() {
        testOrder.setOrderStatus("DELIVERED");
        when(orderRepository.findByOrderNumber("MM-20261008-1001")).thenReturn(Optional.of(testOrder));

        OrderStatusUpdateDto dto = new OrderStatusUpdateDto("CANCELLED", "Trying to cancel delivered order");
        assertThrows(BadRequestException.class, () -> orderService.updateOrderStatus("MM-20261008-1001", dto));
    }
}
