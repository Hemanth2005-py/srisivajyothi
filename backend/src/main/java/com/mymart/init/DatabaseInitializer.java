package com.mymart.init;

import com.mymart.model.*;
import com.mymart.repository.*;
import com.mymart.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;
    private final OrderRepository orderRepository;

    @Override
    public void run(String... args) {
        log.info("Checking database initialization status...");

        // 1. Seed Users
        seedUsers();

        // 2. Seed Categories & Brands & Products if empty
        if (categoryRepository.count() == 0 || productRepository.count() == 0) {
            log.info("Seeding initial categories, brands, products, and inventory...");
            seedCatalog();
        }

        // 3. Seed sample past 6-month orders if none exist
        if (orderRepository.count() == 0) {
            log.info("Seeding historical 6-month orders for analytics...");
            seedHistoricalOrders();
        }

        log.info("Database verification and initialization completed successfully.");
    }

    private void seedUsers() {
        User existingAdmin = userRepository.findByEmailIgnoreCase("srisivajyothi@ssj")
                .or(() -> userRepository.findByEmailIgnoreCase("admin@mymart.com"))
                .orElse(null);

        if (existingAdmin == null) {
            User admin = User.builder()
                    .firebaseUid("admin-firebase-uid-001")
                    .fullName("Sri Siva Jyothi")
                    .username("srisivajyothi")
                    .email("srisivajyothi@ssj")
                    .password("SSJ@SSJ")
                    .phone("9876500001")
                    .role("ROLE_ADMIN")
                    .createdAt(Instant.now().minus(180, ChronoUnit.DAYS))
                    .lastLoginAt(Instant.now())
                    .build();
            userRepository.save(admin);
            log.info("Created default Admin user: srisivajyothi@ssj");
        } else {
            existingAdmin.setEmail("srisivajyothi@ssj");
            existingAdmin.setUsername("srisivajyothi");
            existingAdmin.setFullName("Sri Siva Jyothi");
            existingAdmin.setPassword("SSJ@SSJ");
            userRepository.save(existingAdmin);
            log.info("Synchronized Admin credentials for: srisivajyothi@ssj");
        }

        if (!userRepository.existsByEmailIgnoreCase("customer@mymart.com")) {
            User customer = User.builder()
                    .firebaseUid("customer-firebase-uid-001")
                    .fullName("Rajesh Sharma")
                    .username("customer")
                    .email("customer@mymart.com")
                    .phone("9876543210")
                    .role("ROLE_CUSTOMER")
                    .createdAt(Instant.now().minus(150, ChronoUnit.DAYS))
                    .lastLoginAt(Instant.now())
                    .build();
            userRepository.save(customer);
            log.info("Created default Customer user: customer@mymart.com");
        }
    }

    private void seedCatalog() {
        // Categories
        Category catDairy = saveCategory("Dairy, Bread and Eggs", "Fresh milk, curd, paneer, farm eggs, and morning bread",
                "https://images.unsplash.com/photo-1550583724-b2692b85b150?auto=format&fit=crop&w=400&q=80");
        Category catStaples = saveCategory("Rice, Atta and Pulses", "Flours, premium grains, organic pulses, and lentils",
                "https://images.unsplash.com/photo-1586201375761-83865001e31c?auto=format&fit=crop&w=400&q=80");
        Category catVeg = saveCategory("Fruits and Vegetables", "Farm fresh greens, root vegetables, and seasonal fruits",
                "https://images.unsplash.com/photo-1610832958506-aa56368176cf?auto=format&fit=crop&w=400&q=80");
        Category catSnacks = saveCategory("Snacks and Munchies", "Crunchy namkeen, chips, roasted nuts, and savory bites",
                "https://images.unsplash.com/photo-1566478989037-eec170784d0b?auto=format&fit=crop&w=400&q=80");
        Category catDrinks = saveCategory("Cold Drinks and Juices", "Refreshing cold beverages, fruit juices, and carbonated sodas",
                "https://images.unsplash.com/photo-1551024709-8f23befc6f87?auto=format&fit=crop&w=400&q=80");
        Category catPersonal = saveCategory("Personal Care", "Skincare, haircare, soaps, and oral hygiene essentials",
                "https://images.unsplash.com/photo-1556228720-195a672e8a03?auto=format&fit=crop&w=400&q=80");
        Category catCleaning = saveCategory("Cleaning Supplies", "Surface disinfectants, detergent powders, and cleaners",
                "https://images.unsplash.com/photo-1583947215259-38e31be8751f?auto=format&fit=crop&w=400&q=80");
        Category catBakery = saveCategory("Bakery and Biscuits", "Cookies, rusks, cakes, and artisan bakery delights",
                "https://images.unsplash.com/photo-1509440159596-0249088772ff?auto=format&fit=crop&w=400&q=80");

        // Brands
        Brand bAmul = saveBrand("Amul", "The Taste of India — leading dairy brand", "https://images.unsplash.com/photo-1550583724-b2692b85b150?auto=format&fit=crop&w=200&q=80");
        Brand bTata = saveBrand("Tata", "Trusted consumer goods and nutritional staples", "https://images.unsplash.com/photo-1586201375761-83865001e31c?auto=format&fit=crop&w=200&q=80");
        Brand bBritannia = saveBrand("Britannia", "Eat Healthy, Think Better biscuits and dairy", "https://images.unsplash.com/photo-1558961363-fa8fdf82db35?auto=format&fit=crop&w=200&q=80");
        Brand bFortune = saveBrand("Fortune", "Edible cooking oils and premium grocery staples", "https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?auto=format&fit=crop&w=200&q=80");
        Brand bAashirvaad = saveBrand("Aashirvaad", "Pure chakki whole wheat flours and spices", "https://images.unsplash.com/photo-1509440159596-0249088772ff?auto=format&fit=crop&w=200&q=80");
        Brand bPepsi = saveBrand("Pepsi", "Iconic chilled cola and beverages", "https://images.unsplash.com/photo-1629203851122-3726ecdf080e?auto=format&fit=crop&w=200&q=80");
        Brand bColgate = saveBrand("Colgate", "Oral health and dental care protection", "https://images.unsplash.com/photo-1559599101-f09722fb4948?auto=format&fit=crop&w=200&q=80");
        Brand bDettol = saveBrand("Dettol", "Trusted germ protection and hygiene", "https://images.unsplash.com/photo-1584744982491-665216d95f8b?auto=format&fit=crop&w=200&q=80");
        Brand bHaldiram = saveBrand("Haldiram's", "Traditional Indian namkeens and sweets", "https://images.unsplash.com/photo-1566478989037-eec170784d0b?auto=format&fit=crop&w=200&q=80");
        Brand bFarmFresh = saveBrand("FarmFresh", "Locally harvested supermarket produce", "https://images.unsplash.com/photo-1610832958506-aa56368176cf?auto=format&fit=crop&w=200&q=80");

        // Products
        createProduct("Amul Taaza Homogenised Toned Milk", bAmul.getId(), catDairy.getId(),
                "Pure toned milk, pasteurized and homogenised with consistent nutrition and goodness.",
                1.0, "L", 74.0, 70.0, "SKU-AML-MLK-01",
                "https://images.unsplash.com/photo-1563636619-e9143da7973b?auto=format&fit=crop&w=500&q=80",
                List.of(
                        "https://images.unsplash.com/photo-1563636619-e9143da7973b?auto=format&fit=crop&w=500&q=80",
                        "https://images.unsplash.com/photo-1550583724-b2692b85b150?auto=format&fit=crop&w=500&q=80"
                ), true, true, 80, 35);

        createProduct("Amul Salted Butter", bAmul.getId(), catDairy.getId(),
                "Delicious creamy butter made from pure fresh cream. Perfect on warm toasts and culinary baking.",
                500.0, "g", 275.0, 260.0, "SKU-AML-BTR-01",
                "https://images.unsplash.com/photo-1589985270826-4b7bb135bc9d?auto=format&fit=crop&w=500&q=80",
                List.of(
                        "https://images.unsplash.com/photo-1589985270826-4b7bb135bc9d?auto=format&fit=crop&w=500&q=80",
                        "https://images.unsplash.com/photo-1550583724-b2692b85b150?auto=format&fit=crop&w=500&q=80"
                ), true, true, 60, 25);

        createProduct("Aashirvaad Shudh Superior Whole Wheat Chakki Atta", bAashirvaad.getId(), catStaples.getId(),
                "100% whole wheat grains ground in traditional chakki process for ultra-soft, fluffy rotis.",
                5.0, "kg", 260.0, 235.0, "SKU-ASH-ATT-05",
                "https://images.unsplash.com/photo-1509440159596-0249088772ff?auto=format&fit=crop&w=500&q=80",
                List.of(
                        "https://images.unsplash.com/photo-1509440159596-0249088772ff?auto=format&fit=crop&w=500&q=80",
                        "https://images.unsplash.com/photo-1586201375761-83865001e31c?auto=format&fit=crop&w=500&q=80"
                ), true, true, 45, 18);

        createProduct("Fortune Sunlite Refined Sunflower Cooking Oil", bFortune.getId(), catStaples.getId(),
                "Light, healthy and heart-friendly refined sunflower oil enriched with Vitamins A and D.",
                1.0, "L", 165.0, 142.0, "SKU-FTN-OIL-01",
                "https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?auto=format&fit=crop&w=500&q=80",
                List.of("https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?auto=format&fit=crop&w=500&q=80"),
                true, false, 50, 20);

        createProduct("Tata Sampann Unpolished Toor Dal", bTata.getId(), catStaples.getId(),
                "High-protein unpolished tur dal retaining natural wholesome taste and nutritional benefits.",
                1.0, "kg", 185.0, 168.0, "SKU-TAT-DAL-01",
                "https://images.unsplash.com/photo-1586201375761-83865001e31c?auto=format&fit=crop&w=500&q=80",
                List.of("https://images.unsplash.com/photo-1586201375761-83865001e31c?auto=format&fit=crop&w=500&q=80"),
                false, true, 40, 15);

        createProduct("Fresh Hybrid Tomatoes", bFarmFresh.getId(), catVeg.getId(),
                "Plump, red, and firm hybrid tomatoes freshly harvested from local farm cooperatives.",
                1.0, "kg", 45.0, 38.0, "SKU-VEG-TOM-01",
                "https://images.unsplash.com/photo-1592924357228-91a4daadcfea?auto=format&fit=crop&w=500&q=80",
                List.of("https://images.unsplash.com/photo-1592924357228-91a4daadcfea?auto=format&fit=crop&w=500&q=80"),
                true, true, 70, 30);

        createProduct("Fresh Red Onions", bFarmFresh.getId(), catVeg.getId(),
                "Crisp, spicy, and sharp red onions ideal for Indian curries, salads, and everyday cooking.",
                1.0, "kg", 55.0, 48.0, "SKU-VEG-ONI-01",
                "https://images.unsplash.com/photo-1618512496248-a07fe83aa8cb?auto=format&fit=crop&w=500&q=80",
                List.of("https://images.unsplash.com/photo-1618512496248-a07fe83aa8cb?auto=format&fit=crop&w=500&q=80"),
                false, true, 65, 25);

        createProduct("Fresh Kashmiri Royal Apples", bFarmFresh.getId(), catVeg.getId(),
                "Sweet, juicy, and crunchy premium handpicked red Kashmiri apples loaded with antioxidants.",
                1.0, "kg", 190.0, 165.0, "SKU-FRT-APL-01",
                "https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?auto=format&fit=crop&w=500&q=80",
                List.of("https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?auto=format&fit=crop&w=500&q=80"),
                true, true, 35, 12);

        createProduct("Britannia Good Day Cashew Cookies", bBritannia.getId(), catBakery.getId(),
                "Rich and butter-smooth golden cookies topped with genuine crunchy cashew chunks.",
                200.0, "g", 50.0, 42.0, "SKU-BRT-GDY-01",
                "https://images.unsplash.com/photo-1558961363-fa8fdf82db35?auto=format&fit=crop&w=500&q=80",
                List.of("https://images.unsplash.com/photo-1558961363-fa8fdf82db35?auto=format&fit=crop&w=500&q=80"),
                true, false, 90, 40);

        createProduct("Haldiram's Nagpur Aloo Bhujia", bHaldiram.getId(), catSnacks.getId(),
                "Crispy spiced potato gram flour sev seasoned with signature Indian spices and herbs.",
                400.0, "g", 125.0, 110.0, "SKU-HLD-BHU-01",
                "https://images.unsplash.com/photo-1566478989037-eec170784d0b?auto=format&fit=crop&w=500&q=80",
                List.of("https://images.unsplash.com/photo-1566478989037-eec170784d0b?auto=format&fit=crop&w=500&q=80"),
                false, true, 55, 22);

        createProduct("Pepsi Black Can (Zero Sugar)", bPepsi.getId(), catDrinks.getId(),
                "Bold, crisp and refreshing zero-calorie fizzy cola in a sleek chilled aluminum can.",
                250.0, "ml", 40.0, 35.0, "SKU-PEP-BLK-01",
                "https://images.unsplash.com/photo-1629203851122-3726ecdf080e?auto=format&fit=crop&w=500&q=80",
                List.of("https://images.unsplash.com/photo-1629203851122-3726ecdf080e?auto=format&fit=crop&w=500&q=80"),
                true, false, 100, 45);

        createProduct("Colgate MaxFresh Peppermint Gel Toothpaste", bColgate.getId(), catPersonal.getId(),
                "Cooling crystals deliver intense mint freshness and long-lasting protection against cavities.",
                150.0, "g", 130.0, 115.0, "SKU-CLG-MAX-01",
                "https://images.unsplash.com/photo-1559599101-f09722fb4948?auto=format&fit=crop&w=500&q=80",
                List.of("https://images.unsplash.com/photo-1559599101-f09722fb4948?auto=format&fit=crop&w=500&q=80"),
                false, true, 45, 20);

        createProduct("Dettol Liquid Handwash Refill Pouch", bDettol.getId(), catCleaning.getId(),
                "10x better germ protection formulated with natural pine fragrance and skin moisturizers.",
                750.0, "ml", 149.0, 129.0, "SKU-DTL-HDW-01",
                "https://images.unsplash.com/photo-1584744982491-665216d95f8b?auto=format&fit=crop&w=500&q=80",
                List.of("https://images.unsplash.com/photo-1584744982491-665216d95f8b?auto=format&fit=crop&w=500&q=80"),
                true, true, 60, 25);
    }

    private Category saveCategory(String name, String desc, String img) {
        return categoryRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> categoryRepository.save(Category.builder()
                        .name(name)
                        .description(desc)
                        .imageUrl(img)
                        .active(true)
                        .createdAt(Instant.now().minus(180, ChronoUnit.DAYS))
                        .build()));
    }

    private Brand saveBrand(String name, String desc, String logo) {
        return brandRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> brandRepository.save(Brand.builder()
                        .name(name)
                        .description(desc)
                        .logoUrl(logo)
                        .active(true)
                        .createdAt(Instant.now().minus(180, ChronoUnit.DAYS))
                        .build()));
    }

    private void createProduct(String name, String brandId, String catId, String desc,
                               Double weight, String unit, Double origPrice, Double discPrice,
                               String sku, String mainImg, List<String> images,
                               boolean featured, boolean bestseller, int mainStock, int storeStock) {
        if (!productRepository.existsBySku(sku)) {
            Product p = Product.builder()
                    .name(name)
                    .brandId(brandId)
                    .categoryId(catId)
                    .description(desc)
                    .weight(weight)
                    .unit(unit)
                    .originalPrice(origPrice)
                    .discountPrice(discPrice)
                    .sku(sku)
                    .imageUrl(mainImg)
                    .imageUrls(images)
                    .active(true)
                    .featured(featured)
                    .bestseller(bestseller)
                    .createdAt(Instant.now().minus(120, ChronoUnit.DAYS))
                    .updatedAt(Instant.now())
                    .build();

            Product saved = productRepository.save(p);
            inventoryService.initInventoryForProduct(saved.getId(), sku, mainStock, storeStock);
            log.info("Initialized product {} with Main: {} units, store: {} units", name, mainStock, storeStock);
        }
    }

    private void seedHistoricalOrders() {
        User customer = userRepository.findByEmailIgnoreCase("customer@mymart.com").orElse(null);
        if (customer == null) return;

        List<Product> products = productRepository.findAll();
        if (products.isEmpty()) return;

        Random random = new Random(42);
        Instant now = Instant.now();

        // Generate 35 realistic orders across the past 6 months (180 days)
        for (int i = 0; i < 35; i++) {
            int daysAgo = random.nextInt(170) + 1;
            Instant orderDate = now.minus(daysAgo, ChronoUnit.DAYS).plusSeconds(random.nextInt(3600 * 12));

            DateTimeFormatter orderFmt = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneId.of("Asia/Kolkata"));
            String orderNumber = "MM-" + orderFmt.format(orderDate) + String.format("-%04d", i + 1001);

            int itemCount = random.nextInt(3) + 1;
            List<Order.OrderItem> orderItems = new ArrayList<>();
            double origTotal = 0.0;
            double discSubtotal = 0.0;

            for (int j = 0; j < itemCount; j++) {
                Product p = products.get(random.nextInt(products.size()));
                int qty = random.nextInt(2) + 1;
                double oPrice = p.getOriginalPrice();
                double effPrice = p.getEffectivePrice();
                double sub = effPrice * qty;

                origTotal += oPrice * qty;
                discSubtotal += sub;

                orderItems.add(Order.OrderItem.builder()
                        .productId(p.getId())
                        .productName(p.getName())
                        .sku(p.getSku())
                        .brandName("Brand")
                        .unit(p.getWeight() + " " + p.getUnit())
                        .imageUrl(p.getImageUrl())
                        .quantity(qty)
                        .originalUnitPrice(oPrice)
                        .discountUnitPrice(effPrice)
                        .subtotal(sub)
                        .build());
            }

            double discTotal = Math.max(0.0, origTotal - discSubtotal);
            double delFee = discSubtotal >= 299 ? 0.0 : 35.0;
            double tax = discSubtotal * 0.05;
            double finalTotal = discSubtotal + delFee + tax;

            String status = daysAgo > 3 ? "DELIVERED" : (daysAgo > 1 ? "OUT_FOR_DELIVERY" : "PENDING");
            String payStatus = "DELIVERED".equals(status) ? "PAID" : "PENDING";

            Order order = Order.builder()
                    .orderNumber(orderNumber)
                    .userId(customer.getId())
                    .customerSnapshot(Order.CustomerSnapshot.builder()
                            .name(customer.getFullName())
                            .email(customer.getEmail())
                            .phone(customer.getPhone())
                            .build())
                    .deliveryAddress(Order.DeliveryAddress.builder()
                            .street("Flat 402, Green Glen Layout, Bellandur")
                            .city("Bengaluru")
                            .state("Karnataka")
                            .pincode("560103")
                            .landmark("Near Central Mall")
                            .deliveryInstructions("Leave at door with security")
                            .build())
                    .items(orderItems)
                    .originalTotal(Math.round(origTotal * 100.0) / 100.0)
                    .discountTotal(Math.round(discTotal * 100.0) / 100.0)
                    .deliveryCharge(delFee)
                    .taxTotal(Math.round(tax * 100.0) / 100.0)
                    .finalTotal(Math.round(finalTotal * 100.0) / 100.0)
                    .paymentMethod("CASH_ON_DELIVERY")
                    .paymentStatus(payStatus)
                    .orderStatus(status)
                    .createdAt(orderDate)
                    .updatedAt(orderDate)
                    .build();

            orderRepository.save(order);
        }

        log.info("Successfully seeded 35 historical orders across the last 6 months for analytics testing.");
    }
}
