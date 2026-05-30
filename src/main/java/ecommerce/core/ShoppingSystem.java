package ecommerce.core;

import ecommerce.class_.*;
import ecommerce.interface_.*;
import java.util.ArrayList;
import java.util.List;

public class ShoppingSystem {
    private final List<Product> products;
    private final List<User> users;

    public ShoppingSystem(IProductRepository productRepository) {
        this.products = productRepository != null ? productRepository.loadProducts() : new ArrayList<>();
        this.users = new ArrayList<>();
    }

    public ShoppingSystem() {
        // 保留預設建構子相容性 (但不建議，可視需求移除)
        this.products = new ArrayList<>();
        this.users = new ArrayList<>();
    }

    public void displayProducts() {
        System.out.println("===== 商品清單 =====");
        for (Product p : products) {
            System.out.println(p.getProductId() + ": " + p.getName() + " - $" + p.getPrice() + " (庫存:" + p.getStockQuantity() + ")");
        }
    }

    public User registerUser(String name) {
        String id = String.valueOf(users.size() + 1);
        User user = new User(id, name);
        users.add(user);
        return user;
    }

    public OrderRecord createOrder(User user, IDiscountStrategy discountStrategy, IShippingStrategy shippingStrategy) {
        if (user == null || user.getCart().getItems().isEmpty()) {
            return null;
        }
        List<CartItem> snapshot = new ArrayList<>();
        for (CartItem item : user.getCart().getItems()) {
            snapshot.add(new CartItem(item.getProduct(), item.getQuantity()));
        }
        return new OrderRecord("ORD-" + System.currentTimeMillis(), user, snapshot, discountStrategy, shippingStrategy);
    }

    public Product findProductById(String productId) {
        for (Product p : products) {
            if (p.getProductId().equals(productId)) {
                return p;
            }
        }
        return null;
    }

    // Getters
    public List<Product> getProducts() { return products; }
    public List<User> getUsers() { return users; }
}
