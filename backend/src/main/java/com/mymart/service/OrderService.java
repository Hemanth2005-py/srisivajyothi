package com.mymart.service;

import com.mymart.dto.CreateOrderRequest;
import com.mymart.dto.OrderResponse;
import com.mymart.dto.OrderStatusUpdateDto;
import com.mymart.dto.PagedResponse;
import com.mymart.exception.BadRequestException;
import com.mymart.exception.InsufficientStockException;
import com.mymart.exception.ResourceNotFoundException;
import com.mymart.model.*;
import com.mymart.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final UserRepository userRepository;
    private final InventoryService inventoryService;

    @Value("${app.delivery.free-delivery-threshold:299.0}")
    private double freeDeliveryThreshold;

    @Value("${app.delivery.standard-fee:35.0}")
    private double standardDeliveryFee;

    @Value("${app.delivery.tax-rate:0.05}")
    private double taxRate;

    private static final DateTimeFormatter ORDER_DATE_FMT =
            DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneId.of("Asia/Kolkata"));

    @Transactional
    public synchronized OrderResponse createOrder(String userId, CreateOrderRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        // 1. Gather order items: from request if provided, otherwise from active cart
        List<Order.OrderItem> orderItems = new ArrayList<>();
        List<CreateOrderRequest.OrderItemDto> itemsToProcess = request.getItems();

        if (itemsToProcess == null || itemsToProcess.isEmpty()) {
            Cart cart = cartRepository.findByUserId(userId)
                    .orElseThrow(() -> new BadRequestException("Your shopping cart is empty"));
            if (cart.getItems().isEmpty()) {
                throw new BadRequestException("Your shopping cart is empty");
            }
            itemsToProcess = cart.getItems().stream()
                    .map(ci -> new CreateOrderRequest.OrderItemDto(ci.getProductId(), ci.getQuantity()))
                    .collect(Collectors.toList());
        }

        if (itemsToProcess.isEmpty()) {
            throw new BadRequestException("No items provided for order placement");
        }

        double originalTotal = 0.0;
        double merchandiseSubtotal = 0.0;

        // 2. Validate product prices and stock availability
        for (CreateOrderRequest.OrderItemDto itemDto : itemsToProcess) {
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + itemDto.getProductId()));

            if (!product.isActive()) {
                throw new BadRequestException("Product '" + product.getName() + "' is currently inactive/unavailable");
            }

            int availableStock = inventoryService.getTotalQuantity(product.getId());
            if (availableStock < itemDto.getQuantity()) {
                throw new InsufficientStockException("Insufficient stock for '" + product.getName() +
                        "'. Requested: " + itemDto.getQuantity() + ", Available: " + availableStock);
            }

            String brandName = brandRepository.findById(product.getBrandId())
                    .map(Brand::getName)
                    .orElse("Generic");

            double origPrice = product.getOriginalPrice() != null ? product.getOriginalPrice() : 0.0;
            double effPrice = product.getEffectivePrice();
            double itemSubtotal = round(effPrice * itemDto.getQuantity());

            originalTotal += round(origPrice * itemDto.getQuantity());
            merchandiseSubtotal += itemSubtotal;

            orderItems.add(Order.OrderItem.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .sku(product.getSku())
                    .brandName(brandName)
                    .unit(product.getWeight() != null ? (product.getWeight() + " " + product.getUnit()) : product.getUnit())
                    .imageUrl(product.getImageUrl())
                    .quantity(itemDto.getQuantity())
                    .originalUnitPrice(origPrice)
                    .discountUnitPrice(effPrice)
                    .subtotal(itemSubtotal)
                    .build());
        }

        originalTotal = round(originalTotal);
        merchandiseSubtotal = round(merchandiseSubtotal);
        double discountTotal = round(Math.max(0.0, originalTotal - merchandiseSubtotal));

        boolean freeDelivery = merchandiseSubtotal >= freeDeliveryThreshold;
        double deliveryCharge = freeDelivery ? 0.0 : standardDeliveryFee;
        double taxTotal = round(merchandiseSubtotal * taxRate);
        double finalTotal = round(merchandiseSubtotal + deliveryCharge + taxTotal);

        // 3. Generate unique order number
        String orderNumber = generateUniqueOrderNumber();

        // 4. Atomically deduct inventory
        for (Order.OrderItem oi : orderItems) {
            inventoryService.deductStockForOrder(oi.getProductId(), oi.getQuantity(), orderNumber);
        }

        // 5. Build snapshots
        Order.CustomerSnapshot customerSnapshot = Order.CustomerSnapshot.builder()
                .name(request.getCustomerName() != null ? request.getCustomerName() : user.getFullName())
                .email(user.getEmail())
                .phone(request.getCustomerPhone() != null ? request.getCustomerPhone() : user.getPhone())
                .build();

        Order.DeliveryAddress deliveryAddress = Order.DeliveryAddress.builder()
                .street(request.getStreet())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .landmark(request.getLandmark())
                .deliveryInstructions(request.getDeliveryInstructions())
                .build();

        // 6. Persist order
        Order order = Order.builder()
                .orderNumber(orderNumber)
                .userId(userId)
                .customerSnapshot(customerSnapshot)
                .deliveryAddress(deliveryAddress)
                .items(orderItems)
                .originalTotal(originalTotal)
                .discountTotal(discountTotal)
                .deliveryCharge(deliveryCharge)
                .taxTotal(taxTotal)
                .finalTotal(finalTotal)
                .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "CASH_ON_DELIVERY")
                .paymentStatus("PENDING")
                .orderStatus("PENDING")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        Order savedOrder = orderRepository.save(order);

        // 7. Clear cart after successful order
        cartRepository.deleteByUserId(userId);

        log.info("Order successfully created: {} for user {}", orderNumber, user.getEmail());
        return toOrderResponse(savedOrder);
    }

    public PagedResponse<OrderResponse> getMyOrders(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orderPage = orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        List<OrderResponse> content = orderPage.getContent().stream()
                .map(this::toOrderResponse)
                .collect(Collectors.toList());

        return PagedResponse.<OrderResponse>builder()
                .content(content)
                .pageNumber(page)
                .pageSize(size)
                .totalElements(orderPage.getTotalElements())
                .totalPages(orderPage.getTotalPages())
                .last(orderPage.isLast())
                .build();
    }

    public OrderResponse getOrderByNumber(String orderNumber, String userId, boolean isAdmin) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderNumber));

        if (!isAdmin && !order.getUserId().equals(userId)) {
            throw new BadRequestException("You are not authorized to view this order");
        }

        return toOrderResponse(order);
    }

    public Order getOrderEntityByNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderNumber));
    }

    public PagedResponse<OrderResponse> getAllOrders(int page, int size, String status) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orderPage;
        if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("ALL")) {
            orderPage = orderRepository.findByOrderStatusOrderByCreatedAtDesc(status.toUpperCase(), pageable);
        } else {
            orderPage = orderRepository.findAllByOrderByCreatedAtDesc(pageable);
        }

        List<OrderResponse> content = orderPage.getContent().stream()
                .map(this::toOrderResponse)
                .collect(Collectors.toList());

        return PagedResponse.<OrderResponse>builder()
                .content(content)
                .pageNumber(page)
                .pageSize(size)
                .totalElements(orderPage.getTotalElements())
                .totalPages(orderPage.getTotalPages())
                .last(orderPage.isLast())
                .build();
    }

    public OrderResponse updateOrderStatus(String orderNumber, OrderStatusUpdateDto dto) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderNumber));

        String currentStatus = order.getOrderStatus();
        String targetStatus = dto.getOrderStatus().toUpperCase();

        validateStatusTransition(currentStatus, targetStatus);

        order.setOrderStatus(targetStatus);
        if ("DELIVERED".equals(targetStatus) && "CASH_ON_DELIVERY".equals(order.getPaymentMethod())) {
            order.setPaymentStatus("PAID");
        }
        order.setUpdatedAt(Instant.now());

        Order saved = orderRepository.save(order);
        log.info("Order {} transitioned from {} to {}", orderNumber, currentStatus, targetStatus);
        return toOrderResponse(saved);
    }

    private void validateStatusTransition(String from, String to) {
        if (from.equals(to)) return;

        if ("DELIVERED".equals(from)) {
            throw new BadRequestException("Delivered orders cannot be transitioned to " + to);
        }
        if ("CANCELLED".equals(from)) {
            throw new BadRequestException("Cancelled orders cannot be transitioned to " + to);
        }

        Set<String> validStatuses = Set.of("PENDING", "CONFIRMED", "PROCESSING", "PACKED", "OUT_FOR_DELIVERY", "DELIVERED", "CANCELLED");
        if (!validStatuses.contains(to)) {
            throw new BadRequestException("Invalid target order status: " + to);
        }
    }

    private String generateUniqueOrderNumber() {
        String datePrefix = ORDER_DATE_FMT.format(Instant.now());
        String randomSuffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "MM-" + datePrefix + "-" + randomSuffix;
    }

    public OrderResponse toOrderResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUserId())
                .customerSnapshot(order.getCustomerSnapshot())
                .deliveryAddress(order.getDeliveryAddress())
                .items(order.getItems())
                .originalTotal(order.getOriginalTotal())
                .discountTotal(order.getDiscountTotal())
                .deliveryCharge(order.getDeliveryCharge())
                .taxTotal(order.getTaxTotal())
                .finalTotal(order.getFinalTotal())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .orderStatus(order.getOrderStatus())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    private double round(double val) {
        return BigDecimal.valueOf(val).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
