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
        if (p == null || quantity <= 0) {
            return;
        }
        for (CartItem item : items) {
            if (item.getProduct().getProductId().equals(p.getProductId())) {
                item.setQuantity(item.getQuantity() + quantity);
                return;
            }
        }
        items.add(new CartItem(p, quantity));
    }

    // 透過 ProductID 移除商品
    public void removeItem(String productId) {
        items.removeIf(item -> item.getProduct().getProductId().equals(productId));
    }

    // 計算購物車所有商品總小計
    public double getSubtotal() {
        return items.stream().mapToDouble(CartItem::getItemSubtotal).sum();
    }

    // 清空購物車
    public void clear() {
        // 清空購物車
        items.clear();
    }

    // 取得購物車內所有項目
    public List<CartItem> getItems() {
        return items;
    }
}
