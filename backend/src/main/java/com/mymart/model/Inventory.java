package com.mymart.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "inventory")
@CompoundIndexes({
        @CompoundIndex(name = "product_location_unique", def = "{'productId': 1, 'location': 1}", unique = true)
})
public class Inventory {
    public static final String LOCATION_MAIN = "Main";
    public static final String LOCATION_STORE = "store";

    @Id
    private String id;

    @Indexed
    private String productId;

    @Indexed
    private String sku;

    @Indexed
    private String location; // Must be "Main" or "store"

    private int quantity;

    @Builder.Default
    private int lowStockThreshold = 10;

    @LastModifiedDate
    private Instant updatedAt;

    public static boolean isValidLocation(String loc) {
        return LOCATION_MAIN.equals(loc) || LOCATION_STORE.equals(loc);
    }
}
