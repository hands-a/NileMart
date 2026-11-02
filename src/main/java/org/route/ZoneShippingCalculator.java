package org.route;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
@Component
@Primary
public class ZoneShippingCalculator implements ShippingCalculator {

    @Override
    public BigDecimal calculate(Governorate g) {
        return g.getZone().getFee().setScale(2, RoundingMode.HALF_UP);
    }
}
