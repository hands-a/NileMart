package org.route;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component("freeShipping")
public class FreeShippingCalculator implements ShippingCalculator {

    @Override
    public BigDecimal calculate(Governorate g) {
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }
}
