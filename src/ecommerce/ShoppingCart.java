package ecommerce;

import java.util.ArrayList;
import java.util.List;

public class ShoppingCart {
    private List<CartItem> items;

    public ShoppingCart() {
        this.items = new ArrayList<>();
    }

    public void addItem(Product p, int quantity) {
        // TODO: 加入商品至購物車
    }

    public void removeItem(String productId) {
        // TODO: 透過 ProductID 移除商品
    }

    public double getSubtotal() {
        // TODO: 計算購物車所有商品總小計
        return 0.0;
    }

    public void clear() {
        // TODO: 清空購物車
        items.clear();
    }

    public List<CartItem> getItems() {
        return items;
    }
}
