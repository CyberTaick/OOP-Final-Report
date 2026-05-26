package ecommerce.class_;

import java.util.ArrayList;
import java.util.List;

public class ShoppingCart {
    private final List<CartItem> items;

    // 建構子
    public ShoppingCart() {
        this.items = new ArrayList<>();
    }

    // 加入商品至購物車
    public void addItem(Product p, int quantity) {
        // TODO: 加入商品至購物車
    }

    // 透過 ProductID 移除商品
    public void removeItem(String productId) {
        // TODO: 透過 ProductID 移除商品
    }

    // 計算購物車所有商品總小計
    public double getSubtotal() {
        // TODO: 計算購物車所有商品總小計
        return 0.0;
    }

    // 清空購物車
    public void clear() {
        // TODO: 清空購物車
        items.clear();
    }

    // 取得購物車內所有項目
    public List<CartItem> getItems() {
        return items;
    }
}
