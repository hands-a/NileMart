package org.route;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class PromotionalPricingService implements PricingService {

    private final BigDecimal discountAmount;

    public PromotionalPricingService(@Value("${store.discount-amount:20}") BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }
    @Override
    public BigDecimal getPrice(Product product, int qty) {
        BigDecimal gross = product.getPrice().multiply(BigDecimal.valueOf(qty));
        BigDecimal off = gross.multiply(discountAmount).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return gross.subtract(off).setScale(2, RoundingMode.HALF_UP);
    }
}
