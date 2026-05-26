package ecommerce;

import java.util.ArrayList;
import java.util.List;

public class ShoppingSystem {
    private List<Product> products;
    private List<User> users;

    public ShoppingSystem() {
        this.products = new ArrayList<>();
        this.users = new ArrayList<>();
    }

    public void displayProducts() {
        // TODO: 印出所有商品清單
    }

    public User registerUser(String name) {
        // TODO: 註冊並回傳新用戶
        return null;
    }

    public Order createOrder(User user) {
        // TODO: 為用戶目前的購物車建立訂單
        return null;
    }

    // Getters
    public List<Product> getProducts() { return products; }
    public List<User> getUsers() { return users; }
}
