package org.route;

import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class OrderRegistry {

    private final List<Order> orders = new ArrayList<>();
    private final AtomicInteger sequence = new AtomicInteger(1);
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    public synchronized String createOrderNumber() {
        String dateStr = LocalDate.now().format(dateFormatter);
        return String.format("NM-%s-%05d", dateStr, sequence.getAndIncrement());
    }

    public synchronized void saveOrder(Order order) {
        orders.add(order);
    }

    public synchronized List<Order> getOrders() {
        return Collections.unmodifiableList(new ArrayList<>(orders));
    }

    @PreDestroy
    public void printSummary() {
        int count = orders.size();
        BigDecimal revenue = orders.stream()
                .map(Order::total)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        DecimalFormat df = new DecimalFormat("#,##", DecimalFormatSymbols.getInstance(Locale.US));
        String orderWord = (count == 1) ? "order" : "orders";
        System.out.println("End of day: " + count + " " + orderWord + ", revenue " + df.format(revenue) + " EGP");
    }
}
