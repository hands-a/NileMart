package org.route;

import java.math.BigDecimal;

public enum Zone {
    GREATER_CAIRO(new BigDecimal("50.00"), true),
    ALEX_DELTA(new BigDecimal("65.00"), true),
    CANAL(new BigDecimal("70.00"), true),
    UPPER_EGYPT(new BigDecimal("85.00"), true),
    REMOTE(new BigDecimal("120.00"), false);

    private final BigDecimal fee;
    private final boolean freeShipping;

    Zone(BigDecimal fee, boolean freeShipping) {
        this.fee = fee;
        this.freeShipping = freeShipping;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public boolean isFreeShipping() {
        return freeShipping;
    }
}
