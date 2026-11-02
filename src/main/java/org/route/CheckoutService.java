package org.route;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final ProductService productService;
    private final ShippingQuoteService shippingQuoteService;
    private final TaxCalculator taxCalculator;
    private final OrderRegistry orderRegistry;
    private final List<NotificationSender> notificationSenders;

    public Order placeOrder(ShoppingCart cart, Governorate governorate) {
        if (cart.isEmpty()) {
            throw new StoreException("Cannot checkout an empty cart.");
        }

        for (Map.Entry<String, Integer> line : cart.getLines().entrySet()) {
            Product p = productService.findBySku(line.getKey());
            if (p.getStockQuantity() < line.getValue()) {
                throw new StoreException("Insufficient stock for product " + p.getName() + " (" + p.getSku() + "): only " + p.getStockQuantity() + " available.");
            }
        }

        BigDecimal subtotal = cart.subtotal();
        BigDecimal vat = taxCalculator.calculateVat(subtotal);
        BigDecimal shippingFee = shippingQuoteService.quote(governorate, subtotal);
        BigDecimal total = subtotal.add(vat).add(shippingFee).setScale(2, RoundingMode.HALF_UP);

        // 4. reduces stock for all lines or none (ORD-06)
        for (Map.Entry<String, Integer> line : cart.getLines().entrySet()) {
            productService.reduceStock(line.getKey(), line.getValue());
        }

        // 5. saves the order
        String orderNumber = orderRegistry.createOrderNumber();
        Order order = new Order(
                orderNumber,
                cart.getCustomer(),
                governorate,
                new LinkedHashMap<>(cart.getLines()),
                subtotal,
                vat,
                shippingFee,
                total
        );
        orderRegistry.saveOrder(order);
        DecimalFormat df = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US));
        String message = "Order " + orderNumber + " confirmed. Total " + df.format(total) + " EGP";
        for (NotificationSender sender : notificationSenders) {
            sender.send(cart.getCustomer(), message);
        }
        cart.clear();

        return order;
    }
}
