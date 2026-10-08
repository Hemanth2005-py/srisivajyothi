package com.mymart.repository;

import com.mymart.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {
    Optional<Product> findBySku(String sku);
    boolean existsBySku(String sku);
    Page<Product> findByActiveTrue(Pageable pageable);
    Page<Product> findByCategoryIdAndActiveTrue(String categoryId, Pageable pageable);
    Page<Product> findByBrandIdAndActiveTrue(String brandId, Pageable pageable);
    List<Product> findByActiveTrueAndFeaturedTrue();
    List<Product> findByActiveTrueAndBestsellerTrue();
    long countByActiveTrue();
    List<Product> findByCategoryId(String categoryId);
    List<Product> findByBrandId(String brandId);
}
