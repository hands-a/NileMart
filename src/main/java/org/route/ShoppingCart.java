package org.route;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@Scope("prototype")
public class ShoppingCart {

    private int cartNumber;
    private Customer customer;
    private final Map<String, Integer> lines = new LinkedHashMap<>();

    private final ProductService productService;
    private final int maxQuantityPerProduct;

    public ShoppingCart(ProductService productService,
                        @Value("${store.max-quantity-per-product}") int maxQuantityPerProduct) {
        this.productService = productService;
        this.maxQuantityPerProduct = maxQuantityPerProduct;
    }

    public void addItem(String sku, int qty) {
        if (qty <= 0) {
            throw new StoreException("Quantity must be greater than 0.");
        }
        Product product = productService.findBySku(sku);

        if (product.getStockQuantity() <= 0) {
            throw new StoreException("Cannot add product " + sku + ": out of stock.");
        }

        int currentQty = lines.getOrDefault(sku, 0);
        int newQty = currentQty + qty;

        if (newQty > maxQuantityPerProduct) {
            throw new StoreException("Cannot add " + qty + " of " + product.getName() + ": maximum allowed quantity is " + maxQuantityPerProduct + ".");
        }
        if (newQty > product.getStockQuantity()) {
            throw new StoreException("Cannot add " + qty + " of " + product.getName() + ": only " + product.getStockQuantity() + " available.");
        }

        lines.put(sku, newQty);
    }

    public BigDecimal subtotal() {
        BigDecimal sum = BigDecimal.ZERO;
        for (Map.Entry<String, Integer> entry : lines.entrySet()) {
            sum = sum.add(productService.getPrice(entry.getKey(), entry.getValue()));
        }
        return sum.setScale(2, RoundingMode.HALF_UP);
    }

    public boolean isEmpty() {
        return lines.isEmpty();
    }

    public void clear() {
        lines.clear();
    }

    public int getCartNumber() {
        return cartNumber;
    }

    public void setCartNumber(int cartNumber) {
        this.cartNumber = cartNumber;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Map<String, Integer> getLines() {
        return Collections.unmodifiableMap(lines);
    }
}
