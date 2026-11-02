package org.route;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ShippingQuoteService {

    private final ShippingCalculator primaryCalculator;
    private final ShippingCalculator freeShippingCalculator;
    private final BigDecimal freeShippingThreshold;

    public ShippingQuoteService(ShippingCalculator primaryCalculator,
                                @Qualifier("freeShipping") ShippingCalculator freeShippingCalculator,
                                @Value("${store.free-shipping-threshold}") BigDecimal freeShippingThreshold) {
        this.primaryCalculator = primaryCalculator;
        this.freeShippingCalculator = freeShippingCalculator;
        this.freeShippingThreshold = freeShippingThreshold;
    }

    public BigDecimal quote(Governorate g, BigDecimal subtotal) {
        if (subtotal != null && subtotal.compareTo(freeShippingThreshold) >= 0 && g.getZone().isFreeShipping()) {
            return freeShippingCalculator.calculate(g);
        }
        return primaryCalculator.calculate(g);
    }
}
