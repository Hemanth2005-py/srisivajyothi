package com.mymart.service;

import com.mymart.dto.ProductResponse;
import com.mymart.dto.WishlistResponse;
import com.mymart.exception.ResourceNotFoundException;
import com.mymart.model.Product;
import com.mymart.model.Wishlist;
import com.mymart.repository.ProductRepository;
import com.mymart.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;

    public Wishlist getOrCreateWishlist(String userId) {
        return wishlistRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Wishlist w = Wishlist.builder()
                            .userId(userId)
                            .productIds(new HashSet<>())
                            .updatedAt(Instant.now())
                            .build();
                    return wishlistRepository.save(w);
                });
    }

    public WishlistResponse getWishlist(String userId) {
        Wishlist wishlist = getOrCreateWishlist(userId);
        List<ProductResponse> products = new ArrayList<>();

        for (String pid : wishlist.getProductIds()) {
            productRepository.findById(pid).ifPresent(p -> {
                if (p.isActive()) {
                    products.add(productService.toResponse(p));
                }
            });
        }

        return WishlistResponse.builder()
                .products(products)
                .count(products.size())
                .build();
    }

    public WishlistResponse addToWishlist(String userId, String productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }

        Wishlist wishlist = getOrCreateWishlist(userId);
        wishlist.getProductIds().add(productId);
        wishlist.setUpdatedAt(Instant.now());
        wishlistRepository.save(wishlist);

        return getWishlist(userId);
    }

    public WishlistResponse removeFromWishlist(String userId, String productId) {
        Wishlist wishlist = getOrCreateWishlist(userId);
        wishlist.getProductIds().remove(productId);
        wishlist.setUpdatedAt(Instant.now());
        wishlistRepository.save(wishlist);

        return getWishlist(userId);
    }

    public boolean toggleWishlist(String userId, String productId) {
        Wishlist wishlist = getOrCreateWishlist(userId);
        boolean added;
        if (wishlist.getProductIds().contains(productId)) {
            wishlist.getProductIds().remove(productId);
            added = false;
        } else {
            if (!productRepository.existsById(productId)) {
                throw new ResourceNotFoundException("Product not found with id: " + productId);
            }
            wishlist.getProductIds().add(productId);
            added = true;
        }
        wishlist.setUpdatedAt(Instant.now());
        wishlistRepository.save(wishlist);
        return added;
    }

    public WishlistResponse syncGuestWishlist(String userId, List<String> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return getWishlist(userId);
        }

        Wishlist wishlist = getOrCreateWishlist(userId);
        for (String pid : productIds) {
            if (productRepository.existsById(pid)) {
                wishlist.getProductIds().add(pid);
            }
        }
        wishlist.setUpdatedAt(Instant.now());
        wishlistRepository.save(wishlist);

        return getWishlist(userId);
    }
}
