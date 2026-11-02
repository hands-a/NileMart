package org.route;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.*;

@Repository
public class InMemoryProductRepository implements ProductRepository {

    private final Map<String, Product> store = new LinkedHashMap<>();

    @PostConstruct
    public void init() {
        save(Product.builder()
                .sku("PH-X5-128")
                .name("Smartphone X5 128GB")
                .category("Electronics")
                .price(new BigDecimal("16000.00"))
                .stockQuantity(12)
                .isActive(true)
                .build());

        save(Product.builder()
                .sku("ACC-CHG-20W")
                .name("Fast Charger 20W")
                .category("Electronics")
                .price(new BigDecimal("650.00"))
                .stockQuantity(40)
                .isActive(true)
                .build());

        save(Product.builder()
                .sku("ACC-BUDS-6")
                .name("Wireless Earbuds")
                .category("Electronics")
                .price(new BigDecimal("1100.00"))
                .stockQuantity(8)
                .isActive(true)
                .build());

        save(Product.builder()
                .sku("FSH-GAL-001")
                .name("Cotton Galabeya")
                .category("Fashion")
                .price(new BigDecimal("900.00"))
                .stockQuantity(25)
                .isActive(true)
                .build());

        save(Product.builder()
                .sku("HOM-PAN-28")
                .name("Non-stick Frying Pan 28cm")
                .category("Home & Kitchen")
                .price(new BigDecimal("1200.00"))
                .stockQuantity(3)
                .isActive(true)
                .build());

        save(Product.builder()
                .sku("HOM-SPC-RCK")
                .name("Wooden Spice Rack")
                .category("Home & Kitchen")
                .price(new BigDecimal("750.00"))
                .stockQuantity(10)
                .isActive(true)
                .build());

        save(Product.builder()
                .sku("BTY-SUN-50")
                .name("Sun Cream SPF50")
                .category("Beauty")
                .price(new BigDecimal("350.00"))
                .stockQuantity(0)
                .isActive(true)
                .build());

        save(Product.builder()
                .sku("BK-CAI-TRI")
                .name("The Cairo Trilogy")
                .category("Books")
                .price(new BigDecimal("450.00"))
                .stockQuantity(15)
                .isActive(true)
                .build());
    }

    @PreDestroy
    public void destroy() {
        System.out.println("Catalog closed");
    }

    @Override
    public List<Product> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public Optional<Product> findBySku(String sku) {
        return Optional.ofNullable(store.get(sku));
    }

    @Override
    public Product save(Product product) {
        store.put(product.getSku(), product);
        return product;
    }
}
