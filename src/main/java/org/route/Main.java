package org.route;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

public class Main {

    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("###", DecimalFormatSymbols.getInstance(Locale.US));

    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);

        ProductService productService = context.getBean(ProductService.class);
        CheckoutService checkoutService = context.getBean(CheckoutService.class);

        Map<Integer, ShoppingCart> carts = new LinkedHashMap<>();
        int nextCartId = 1;

        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println();
            System.out.println("1. Show products");
            System.out.println("2. Create a cart");
            System.out.println("3. Add a product to a cart");
            System.out.println("4. Show a cart");
            System.out.println("5. Check out a cart");
            System.out.println("0. Exit");
            System.out.print("Choose an option: ");

            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                continue;
            }

            try {
                switch (input) {
                    case "1":
                        showProducts(productService);
                        break;
                    case "2":
                        int cartId = nextCartId++;
                        createCart(context, carts, cartId, scanner);
                        break;
                    case "3":
                        addProductToCart(carts, scanner);
                        break;
                    case "4":
                        showCart(carts, productService, scanner);
                        break;
                    case "5":
                        checkoutCart(carts, checkoutService, scanner);
                        break;
                    case "0":
                        running = false;
                        break;
                    default:
                        System.out.println("Invalid option. Please enter a number between 0 and 5.");
                }
            } catch (StoreException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

        context.close();
    }

    private static void showProducts(ProductService productService) {
        List<Product> products = productService.findAll();
        System.out.println("\n--- Product Catalog ---");
        for (Product p : products) {
            System.out.printf("[%s] %-25s | Category: %-15s | Price: %s EGP | Stock: %d%n",
                    p.getSku(),
                    p.getName(),
                    p.getCategory(),
                    MONEY_FORMAT.format(p.getPrice()),
                    p.getStockQuantity());
        }
    }

    private static void createCart(AnnotationConfigApplicationContext context,
                                   Map<Integer, ShoppingCart> carts,
                                   int cartId,
                                   Scanner scanner) {
        System.out.print("Enter customer full name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            throw new StoreException("Customer name cannot be empty.");
        }

        System.out.print("Enter customer phone: ");
        String phone = scanner.nextLine().trim();
        if (phone.isEmpty()) {
            throw new StoreException("Customer phone cannot be empty.");
        }

        System.out.print("Enter customer email (optional, press Enter to skip): ");
        String email = scanner.nextLine().trim();
        if (email.isEmpty()) {
            email = null;
        }

        Customer customer = new Customer(name, phone, email);
        ShoppingCart cart = context.getBean(ShoppingCart.class);
        cart.setCartNumber(cartId);
        cart.setCustomer(customer);
        carts.put(cartId, cart);

        System.out.println("Cart #" + cartId + " created successfully for " + name + ".");
    }

    private static void addProductToCart(Map<Integer, ShoppingCart> carts, Scanner scanner) {
        System.out.print("Enter Cart ID: ");
        int cartId = parseInteger(scanner.nextLine().trim(), "Cart ID");
        ShoppingCart cart = carts.get(cartId);
        if (cart == null) {
            throw new StoreException("Cart #" + cartId + " not found.");
        }

        System.out.print("Enter Product SKU: ");
        String sku = scanner.nextLine().trim();

        System.out.print("Enter Quantity: ");
        int qty = parseInteger(scanner.nextLine().trim(), "Quantity");

        cart.addItem(sku, qty);
        System.out.println("Successfully added " + qty + " item(s) to Cart #" + cartId + ".");
    }

    private static void showCart(Map<Integer, ShoppingCart> carts, ProductService productService, Scanner scanner) {
        System.out.print("Enter Cart ID: ");
        int cartId = parseInteger(scanner.nextLine().trim(), "Cart ID");
        ShoppingCart cart = carts.get(cartId);
        if (cart == null) {
            throw new StoreException("Cart #" + cartId + " not found.");
        }

        Customer c = cart.getCustomer();
        System.out.println("\n--- Cart #" + cartId + " ---");
        if (c != null) {
            System.out.println("Customer: " + c.fullName() + " | Phone: " + c.phone() + " | Email: " + (c.email() != null ? c.email() : "N/A"));
        }

        if (cart.isEmpty()) {
            System.out.println("Cart is empty.");
            return;
        }

        System.out.println("Items:");
        for (Map.Entry<String, Integer> line : cart.getLines().entrySet()) {
            Product product = productService.findBySku(line.getKey());
            var linePrice = productService.getPrice(product.getSku(), line.getValue());
            System.out.printf(" - %-25s x %d = %s EGP%n",
                    product.getName(),
                    line.getValue(),
                    MONEY_FORMAT.format(linePrice));
        }
        System.out.println("Subtotal: " + MONEY_FORMAT.format(cart.subtotal()) + " EGP");
    }

    private static void checkoutCart(Map<Integer, ShoppingCart> carts, CheckoutService checkoutService, Scanner scanner) {
        System.out.print("Enter Cart ID: ");
        int cartId = parseInteger(scanner.nextLine().trim(), "Cart ID");
        ShoppingCart cart = carts.get(cartId);
        if (cart == null) {
            throw new StoreException("Cart #" + cartId + " not found.");
        }

        System.out.print("Enter shipping Governorate (e.g. Cairo, Giza, Aswan): ");
        String govInput = scanner.nextLine().trim();
        Governorate governorate = Governorate.fromString(govInput);

        Order order = checkoutService.placeOrder(cart, governorate);
        System.out.println("\n--- Order Summary ---");
        System.out.println("Order Number: " + order.orderNumber());
        System.out.println("Governorate: " + order.governorate().getDisplayName() + " (" + order.governorate().getZone() + ")");
        System.out.println("Subtotal: " + MONEY_FORMAT.format(order.subtotal()) + " EGP");
        System.out.println("VAT: " + MONEY_FORMAT.format(order.vat()) + " EGP");
        System.out.println("Shipping: " + MONEY_FORMAT.format(order.shippingFee()) + " EGP");
        System.out.println("Total: " + MONEY_FORMAT.format(order.total()) + " EGP");
    }

    private static int parseInteger(String str, String fieldName) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            throw new StoreException("Invalid " + fieldName + ": must be a valid number.");
        }
    }
}
