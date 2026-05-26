package ecommerce.core;

import java.util.ArrayList;
import java.util.List;

import ecommerce.class_.*;
import ecommerce.interface_.*;

public class ShoppingSystem {
    private final List<Product> products;
    private final List<User> users;

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

    public Order createOrder(User user, IDiscountStrategy discountStrategy, IShippingStrategy shippingStrategy) {
        // TODO: 為用戶目前的購物車建立訂單
        return null;
    }

    // Getters
    public List<Product> getProducts() { return products; }
    public List<User> getUsers() { return users; }
}
