package org.route;

import org.springframework.stereotype.Component;

@Component
public class EmailNotificationSender implements NotificationSender {

    @Override
    public void send(Customer customer, String message) {
        if (customer != null && customer.email() != null && !customer.email().isBlank()) {
            System.out.println("[EMAIL] to " + customer.email() + ": " + message);
        }
    }
}
