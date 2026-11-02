package org.route;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

public class NileMartTest {

    private AnnotationConfigApplicationContext context;
    private ProductService productService;
    private CheckoutService checkoutService;

    @BeforeEach
    void setUp() {
        context = new AnnotationConfigApplicationContext(AppConfig.class);
        productService = context.getBean(ProductService.class);
        checkoutService = context.getBean(CheckoutService.class);
    }

    @AfterEach
    void tearDown() {
        if (context != null && context.isActive()) {
            context.close();
        }
    }

    @Test
    @DisplayName("Test Scenario 1: Worked Example 1 (Standard pricing, Giza)")
    void testWorkedExample1() {
        // 2 x Fast Charger 20W (650 ea), 1 x Cotton Galabeya (900 ea)
        ShoppingCart cart = context.getBean(ShoppingCart.class);
        Customer customer = new Customer("Mona", "01012345678", "mona@example.com");
        cart.setCustomer(customer);

        cart.addItem("ACC-CHG-20W", 2);
        cart.addItem("FSH-GAL-001", 1);

        Order order = checkoutService.placeOrder(cart, Governorate.GIZA);

        assertEquals(new BigDecimal("2200.00"), order.subtotal());
        assertEquals(new BigDecimal("308.00"), order.vat());
        assertEquals(new BigDecimal("50.00"), order.shippingFee());
        assertEquals(new BigDecimal("2558.00"), order.total());
    }

    @Test
    @DisplayName("Test Scenario 1: Worked Example 2 (Promo pricing 20% off, Giza)")
    void testWorkedExample2() {
        // With Promo pricing (20% off)
        PricingService promoPricing = new PromotionalPricingService(new BigDecimal("20"));
        ProductRepository repo = context.getBean(ProductRepository.class);
        ProductService promoProductService = new ProductService(repo, promoPricing);

        ShoppingCart cart = new ShoppingCart(promoProductService, 10);
        Customer customer = new Customer("Mona", "01012345678", "mona@example.com");
        cart.setCustomer(customer);

        cart.addItem("ACC-CHG-20W", 2);
        cart.addItem("FSH-GAL-001", 1);

        TaxCalculator taxCalc = context.getBean(TaxCalculator.class);
        ShippingQuoteService shippingQuoteService = context.getBean(ShippingQuoteService.class);
        OrderRegistry orderRegistry = context.getBean(OrderRegistry.class);

        CheckoutService promoCheckoutService = new CheckoutService(
                promoProductService,
                shippingQuoteService,
                taxCalc,
                orderRegistry,
                java.util.List.of()
        );

        Order order = promoCheckoutService.placeOrder(cart, Governorate.GIZA);

        assertEquals(new BigDecimal("1760.00"), order.subtotal());
        assertEquals(new BigDecimal("246.40"), order.vat());
        assertEquals(new BigDecimal("50.00"), order.shippingFee());
        assertEquals(new BigDecimal("2056.40"), order.total());
    }

    @Test
    @DisplayName("Test Scenario 1: Worked Example 3 (Aswan >= 5000 EGP, Free shipping)")
    void testWorkedExample3() {
        // 1 x Smartphone X5 128GB (16,000) to Aswan (UPPER_EGYPT, eligible for free shipping)
        ShoppingCart cart = context.getBean(ShoppingCart.class);
        cart.setCustomer(new Customer("Ali", "01112223334", "ali@example.com"));

        cart.addItem("PH-X5-128", 1);

        Order order = checkoutService.placeOrder(cart, Governorate.ASWAN);

        assertEquals(new BigDecimal("16000.00"), order.subtotal());
        assertEquals(new BigDecimal("2240.00"), order.vat());
        assertEquals(new BigDecimal("0.00"), order.shippingFee());
        assertEquals(new BigDecimal("18240.00"), order.total());
    }

    @Test
    @DisplayName("Test Scenario 1: Worked Example 4 (South Sinai >= 5000 EGP, Remote zone fee applies)")
    void testWorkedExample4() {
        // 1 x Smartphone X5 128GB (16,000) to South Sinai (REMOTE, not eligible for free shipping)
        ShoppingCart cart = context.getBean(ShoppingCart.class);
        cart.setCustomer(new Customer("Tarek", "01234567890", "tarek@example.com"));

        cart.addItem("PH-X5-128", 1);

        Order order = checkoutService.placeOrder(cart, Governorate.SOUTH_SINAI);

        assertEquals(new BigDecimal("16000.00"), order.subtotal());
        assertEquals(new BigDecimal("2240.00"), order.vat());
        assertEquals(new BigDecimal("120.00"), order.shippingFee());
        assertEquals(new BigDecimal("18360.00"), order.total());
    }

    @Test
    @DisplayName("Test Scenario 2: Adding Sun Cream SPF50 fails: out of stock")
    void testScenario2OutOfStock() {
        ShoppingCart cart = context.getBean(ShoppingCart.class);
        StoreException ex = assertThrows(StoreException.class, () -> {
            cart.addItem("BTY-SUN-50", 1);
        });
        assertTrue(ex.getMessage().contains("out of stock") || ex.getMessage().contains("0"));
    }

    @Test
    @DisplayName("Test Scenario 3: Adding 4 Non-stick Frying Pans fails: only 3 available")
    void testScenario3ExceedsAvailableStock() {
        ShoppingCart cart = context.getBean(ShoppingCart.class);
        StoreException ex = assertThrows(StoreException.class, () -> {
            cart.addItem("HOM-PAN-28", 4);
        });
        assertTrue(ex.getMessage().contains("only 3 available") || ex.getMessage().contains("3"));
    }

    @Test
    @DisplayName("Test Scenario 4: Two carts for two customers stay separate (prototype scope)")
    void testScenario4TwoCartsStaySeparate() {
        ShoppingCart cart1 = context.getBean(ShoppingCart.class);
        ShoppingCart cart2 = context.getBean(ShoppingCart.class);

        assertNotSame(cart1, cart2, "Carts should be separate prototype instances");

        cart1.setCustomer(new Customer("Customer 1", "01000000001", null));
        cart2.setCustomer(new Customer("Customer 2", "01000000002", null));

        cart1.addItem("ACC-CHG-20W", 2);
        cart2.addItem("BK-CAI-TRI", 3);

        assertEquals(1, cart1.getLines().size());
        assertEquals(1, cart2.getLines().size());
        assertTrue(cart1.getLines().containsKey("ACC-CHG-20W"));
        assertFalse(cart1.getLines().containsKey("BK-CAI-TRI"));
        assertTrue(cart2.getLines().containsKey("BK-CAI-TRI"));
        assertFalse(cart2.getLines().containsKey("ACC-CHG-20W"));
    }

    @Test
    @DisplayName("Test Scenario 5: Put 2 Wireless Earbuds in cart 1 and 7 in cart 2. Checkout cart 2, then cart 1 fails and stock remains")
    void testScenario5StockRecheckPreventsSecondCheckout() {
        ShoppingCart cart1 = context.getBean(ShoppingCart.class);
        ShoppingCart cart2 = context.getBean(ShoppingCart.class);

        cart1.setCustomer(new Customer("Cust 1", "01011111111", null));
        cart2.setCustomer(new Customer("Cust 2", "01022222222", null));

        // Initial stock of ACC-BUDS-6 is 8
        assertEquals(8, productService.findBySku("ACC-BUDS-6").getStockQuantity());

        cart1.addItem("ACC-BUDS-6", 2);
        cart2.addItem("ACC-BUDS-6", 7);

        // Checkout cart 2 successfully
        Order order2 = checkoutService.placeOrder(cart2, Governorate.CAIRO);
        assertNotNull(order2);

        // Stock should now be 8 - 7 = 1
        assertEquals(1, productService.findBySku("ACC-BUDS-6").getStockQuantity());

        // Checkout cart 1: needs 2, but only 1 is available -> fails!
        StoreException ex = assertThrows(StoreException.class, () -> {
            checkoutService.placeOrder(cart1, Governorate.CAIRO);
        });
        assertTrue(ex.getMessage().contains("Insufficient stock"));

        // Stock must remain unchanged at 1
        assertEquals(1, productService.findBySku("ACC-BUDS-6").getStockQuantity());
    }

    @Test
    @DisplayName("Test Scenario 6: Exiting prints end-of-day summary and Catalog closed")
    void testScenario6PreDestroyOutput() {
        // Place two orders to reproduce the worked example summary:
        // Order 1: Example 1 (2,558.00)
        ShoppingCart cart1 = context.getBean(ShoppingCart.class);
        cart1.setCustomer(new Customer("Mona", "01012345678", "mona@example.com"));
        cart1.addItem("ACC-CHG-20W", 2);
        cart1.addItem("FSH-GAL-001", 1);
        checkoutService.placeOrder(cart1, Governorate.GIZA);

        // Order 2: Example 3 (18,240.00)
        ShoppingCart cart2 = context.getBean(ShoppingCart.class);
        cart2.setCustomer(new Customer("Ali", "01112223334", "ali@example.com"));
        cart2.addItem("PH-X5-128", 1);
        checkoutService.placeOrder(cart2, Governorate.ASWAN);

        // Intercept System.out
        PrintStream originalOut = System.out;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        try {
            context.close();
        } finally {
            System.setOut(originalOut);
        }

        String output = outContent.toString();
        assertTrue(output.contains("End of day: 2 orders, revenue 20,798.00 EGP"),
                "Output should contain expected summary: " + output);
        assertTrue(output.contains("Catalog closed"),
                "Output should contain Catalog closed: " + output);
    }

    @Test
    @DisplayName("Test Order Number Format NM-YYYYMMDD-NNNNN")
    void testOrderNumberFormat() {
        ShoppingCart cart = context.getBean(ShoppingCart.class);
        cart.setCustomer(new Customer("Test", "01000000000", null));
        cart.addItem("BK-CAI-TRI", 1);

        Order order = checkoutService.placeOrder(cart, Governorate.CAIRO);
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        assertTrue(order.orderNumber().matches("^NM-" + today + "-\\d{5}$"),
                "Order number should match format: " + order.orderNumber());
    }

    @Test
    @DisplayName("Test Empty Cart Checkout Fails (ORD-02)")
    void testEmptyCartCheckoutFails() {
        ShoppingCart cart = context.getBean(ShoppingCart.class);
        cart.setCustomer(new Customer("Test", "01000000000", null));

        StoreException ex = assertThrows(StoreException.class, () -> {
            checkoutService.placeOrder(cart, Governorate.CAIRO);
        });
        assertTrue(ex.getMessage().contains("empty"));
    }

    @Test
    @DisplayName("Test Max Quantity per Product (CART-03)")
    void testMaxQuantityPerProduct() {
        ShoppingCart cart = context.getBean(ShoppingCart.class);
        // Fast charger has stock 40, but store.max-quantity-per-product=10
        StoreException ex = assertThrows(StoreException.class, () -> {
            cart.addItem("ACC-CHG-20W", 11);
        });
        assertTrue(ex.getMessage().contains("10"));
    }

    @Test
    @DisplayName("Test Notifications: SMS and Email outputs")
    void testNotifications() {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        try {
            ShoppingCart cart = context.getBean(ShoppingCart.class);
            cart.setCustomer(new Customer("Mona", "01012345678", "mona@example.com"));
            cart.addItem("ACC-CHG-20W", 2);
            cart.addItem("FSH-GAL-001", 1);
            checkoutService.placeOrder(cart, Governorate.GIZA);
        } finally {
            System.setOut(originalOut);
        }

        String output = outContent.toString();
        assertTrue(output.contains("[SMS] to 01012345678: Order NM-"), "SMS notification missing: " + output);
        assertTrue(output.contains("[EMAIL] to mona@example.com: Order NM-"), "Email notification missing: " + output);
        assertTrue(output.contains("confirmed. Total 2,558.00 EGP"), "Message missing total: " + output);
    }

    @Test
    @DisplayName("Test Notifications: No Email sent when email is null or blank")
    void testNotificationWithoutEmail() {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        try {
            ShoppingCart cart = context.getBean(ShoppingCart.class);
            cart.setCustomer(new Customer("Ahmed", "01099999999", null));
            cart.addItem("BK-CAI-TRI", 1);
            checkoutService.placeOrder(cart, Governorate.CAIRO);
        } finally {
            System.setOut(originalOut);
        }

        String output = outContent.toString();
        assertTrue(output.contains("[SMS] to 01099999999: Order NM-"));
        assertFalse(output.contains("[EMAIL]"), "Email notification should not be sent when email is null");
    }
}
