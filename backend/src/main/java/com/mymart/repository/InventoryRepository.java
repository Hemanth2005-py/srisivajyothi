package com.mymart.repository;

import com.mymart.model.Inventory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends MongoRepository<Inventory, String> {
    Optional<Inventory> findByProductIdAndLocation(String productId, String location);
    List<Inventory> findByProductId(String productId);
    List<Inventory> findByLocation(String location);
    List<Inventory> findByQuantityLessThanEqual(int threshold);
    List<Inventory> findByQuantityEquals(int quantity);
    void deleteByProductId(String productId);
}
