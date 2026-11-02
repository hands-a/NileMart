package org.route;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    List<Product> findAll();
    Optional<Product> findBySku(String sku);
    Product save(Product product);
}
