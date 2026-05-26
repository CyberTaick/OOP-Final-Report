package ecommerce;

import java.util.List;

public class Order {
    private String orderId;
    private User buyer;
    private List<CartItem> items;
    private IDiscountStrategy discountStrategy;
    private IShippingStrategy shippingStrategy;
    private String status; // 例如: "Pending", "Completed"

    public Order(String orderId, User buyer, List<CartItem> items, 
                 IDiscountStrategy discountStrategy, IShippingStrategy shippingStrategy) {
        this.orderId = orderId;
        this.buyer = buyer;
        this.items = items; // 通常是 ShoppingCart 結帳時建立的一份快照
        this.discountStrategy = discountStrategy;
        this.shippingStrategy = shippingStrategy;
        this.status = "Pending";
    }

    public double calculateTotal() {
        // TODO: 計算最終總金額 (Subtotal - Discount + Shipping)
        return 0.0;
    }

    public boolean checkout(IPaymentProcessor payment) {
        // TODO: 呼叫付款處理邏輯，成功後變更狀態為 Completed
        return false;
    }

    public void printReceipt() {
        // TODO: 印出訂單明細
    }

    // Getters and Setters
    public String getOrderId() { return orderId; }
    public User getBuyer() { return buyer; }
    public List<CartItem> getItems() { return items; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
