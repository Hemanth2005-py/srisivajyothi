package com.mymart.service;

import com.mymart.dto.AddToCartRequest;
import com.mymart.dto.CartItemResponse;
import com.mymart.dto.CartResponse;
import com.mymart.dto.CreateOrderRequest;
import com.mymart.exception.BadRequestException;
import com.mymart.exception.InsufficientStockException;
import com.mymart.exception.ResourceNotFoundException;
import com.mymart.model.Brand;
import com.mymart.model.Cart;
import com.mymart.model.Product;
import com.mymart.repository.BrandRepository;
import com.mymart.repository.CartRepository;
import com.mymart.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final InventoryService inventoryService;

    @Value("${app.delivery.free-delivery-threshold:299.0}")
    private double freeDeliveryThreshold;

    @Value("${app.delivery.standard-fee:35.0}")
    private double standardDeliveryFee;

    @Value("${app.delivery.tax-rate:0.05}")
    private double taxRate;

    public Cart getOrCreateCartEntity(String userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart cart = Cart.builder()
                            .userId(userId)
                            .items(new ArrayList<>())
                            .updatedAt(Instant.now())
                            .build();
                    return cartRepository.save(cart);
                });
    }

    public CartResponse getCart(String userId) {
        Cart cart = getOrCreateCartEntity(userId);
        return buildCartResponse(cart);
    }

    public CartResponse addToCart(String userId, AddToCartRequest request) {
        if (request.getQuantity() <= 0) {
            throw new BadRequestException("Quantity must be greater than 0");
        }

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));

        if (!product.isActive()) {
            throw new BadRequestException("Product is currently unavailable");
        }

        int availableStock = inventoryService.getTotalQuantity(product.getId());
        if (availableStock <= 0) {
            throw new InsufficientStockException("Product '" + product.getName() + "' is out of stock");
        }

        Cart cart = getOrCreateCartEntity(userId);
        Optional<Cart.CartItem> existingItemOpt = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(product.getId()))
                .findFirst();

        int targetQuantity;
        if (existingItemOpt.isPresent()) {
            targetQuantity = existingItemOpt.get().getQuantity() + request.getQuantity();
            if (targetQuantity > availableStock) {
                throw new InsufficientStockException("Cannot add more. Total in cart (" + targetQuantity +
                        ") exceeds available stock (" + availableStock + ")");
            }
            existingItemOpt.get().setQuantity(targetQuantity);
        } else {
            targetQuantity = request.getQuantity();
            if (targetQuantity > availableStock) {
                throw new InsufficientStockException("Requested quantity (" + targetQuantity +
                        ") exceeds available stock (" + availableStock + ")");
            }
            cart.getItems().add(new Cart.CartItem(product.getId(), targetQuantity));
        }

        cart.setUpdatedAt(Instant.now());
        cartRepository.save(cart);

        return buildCartResponse(cart);
    }

    public CartResponse updateCartItem(String userId, String productId, int quantity) {
        Cart cart = getOrCreateCartEntity(userId);

        if (quantity <= 0) {
            // Remove item
            cart.getItems().removeIf(item -> item.getProductId().equals(productId));
        } else {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

            int availableStock = inventoryService.getTotalQuantity(productId);
            if (quantity > availableStock) {
                throw new InsufficientStockException("Quantity (" + quantity + ") exceeds available stock (" + availableStock + ")");
            }

            Optional<Cart.CartItem> existingItemOpt = cart.getItems().stream()
                    .filter(item -> item.getProductId().equals(productId))
                    .findFirst();

            if (existingItemOpt.isPresent()) {
                existingItemOpt.get().setQuantity(quantity);
            } else {
                cart.getItems().add(new Cart.CartItem(productId, quantity));
            }
        }

        cart.setUpdatedAt(Instant.now());
        cartRepository.save(cart);

        return buildCartResponse(cart);
    }

    public CartResponse removeFromCart(String userId, String productId) {
        Cart cart = getOrCreateCartEntity(userId);
        cart.getItems().removeIf(item -> item.getProductId().equals(productId));
        cart.setUpdatedAt(Instant.now());
        cartRepository.save(cart);
        return buildCartResponse(cart);
    }

    public void clearCart(String userId) {
        Cart cart = getOrCreateCartEntity(userId);
        cart.getItems().clear();
        cart.setUpdatedAt(Instant.now());
        cartRepository.save(cart);
    }

    public CartResponse syncGuestCart(String userId, List<CreateOrderRequest.OrderItemDto> guestItems) {
        if (guestItems == null || guestItems.isEmpty()) {
            return getCart(userId);
        }

        Cart cart = getOrCreateCartEntity(userId);
        for (CreateOrderRequest.OrderItemDto guestItem : guestItems) {
            Product product = productRepository.findById(guestItem.getProductId()).orElse(null);
            if (product != null && product.isActive()) {
                int availableStock = inventoryService.getTotalQuantity(product.getId());
                if (availableStock > 0) {
                    Optional<Cart.CartItem> existing = cart.getItems().stream()
                            .filter(i -> i.getProductId().equals(product.getId()))
                            .findFirst();

                    int newQty = guestItem.getQuantity();
                    if (existing.isPresent()) {
                        newQty = Math.max(existing.get().getQuantity(), newQty);
                        existing.get().setQuantity(Math.min(newQty, availableStock));
                    } else {
                        cart.getItems().add(new Cart.CartItem(product.getId(), Math.min(newQty, availableStock)));
                    }
                }
            }
        }

        cart.setUpdatedAt(Instant.now());
        cartRepository.save(cart);
        return buildCartResponse(cart);
    }

    public CartResponse buildCartResponse(Cart cart) {
        List<CartItemResponse> itemResponses = new ArrayList<>();
        double originalTotal = 0.0;
        double merchandiseSubtotal = 0.0;
        int totalItemCount = 0;

        for (Cart.CartItem item : cart.getItems()) {
            Optional<Product> prodOpt = productRepository.findById(item.getProductId());
            if (prodOpt.isPresent() && prodOpt.get().isActive()) {
                Product p = prodOpt.get();
                String brandName = brandRepository.findById(p.getBrandId())
                        .map(Brand::getName)
                        .orElse("Generic");
                int availableStock = inventoryService.getTotalQuantity(p.getId());

                double origPrice = p.getOriginalPrice() != null ? p.getOriginalPrice() : 0.0;
                double effPrice = p.getEffectivePrice();
                double itemSubtotal = round(effPrice * item.getQuantity());

                originalTotal += round(origPrice * item.getQuantity());
                merchandiseSubtotal += itemSubtotal;
                totalItemCount += item.getQuantity();

                itemResponses.add(CartItemResponse.builder()
                        .productId(p.getId())
                        .productName(p.getName())
                        .sku(p.getSku())
                        .brandName(brandName)
                        .imageUrl(p.getImageUrl())
                        .weight(p.getWeight())
                        .unit(p.getUnit())
                        .originalPrice(origPrice)
                        .discountPrice(p.getDiscountPrice())
                        .effectivePrice(effPrice)
                        .discountPercentage(p.getDiscountPercentage())
                        .quantity(item.getQuantity())
                        .subtotal(itemSubtotal)
                        .availableStock(availableStock)
                        .build());
            }
        }

        originalTotal = round(originalTotal);
        merchandiseSubtotal = round(merchandiseSubtotal);
        double discountTotal = round(Math.max(0.0, originalTotal - merchandiseSubtotal));

        boolean freeDeliveryEligible = merchandiseSubtotal >= freeDeliveryThreshold || totalItemCount == 0;
        double deliveryCharge = (totalItemCount == 0 || freeDeliveryEligible) ? 0.0 : standardDeliveryFee;
        double taxTotal = round(merchandiseSubtotal * taxRate);
        double finalPayable = round(merchandiseSubtotal + deliveryCharge + taxTotal);
        double amountNeeded = freeDeliveryEligible ? 0.0 : round(Math.max(0.0, freeDeliveryThreshold - merchandiseSubtotal));

        return CartResponse.builder()
                .items(itemResponses)
                .totalItemCount(totalItemCount)
                .originalTotal(originalTotal)
                .discountTotal(discountTotal)
                .merchandiseSubtotal(merchandiseSubtotal)
                .deliveryCharge(deliveryCharge)
                .taxTotal(taxTotal)
                .finalPayableAmount(finalPayable)
                .freeDeliveryThreshold(freeDeliveryThreshold)
                .freeDeliveryEligible(freeDeliveryEligible)
                .amountNeededForFreeDelivery(amountNeeded)
                .build();
    }

    private double round(double val) {
        return BigDecimal.valueOf(val).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
