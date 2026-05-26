package ecommerce.class_;

import ecommerce.interface_.IDiscountStrategy;
import ecommerce.interface_.IPaymentProcessor;
import ecommerce.interface_.IShippingStrategy;
import java.util.List;

public class Order {
    private final String orderId;
    private final User buyer;
    private final List<CartItem> items;
    private final IDiscountStrategy discountStrategy;
    private final IShippingStrategy shippingStrategy;
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

    // 計算最終總金額 (Subtotal - Discount + Shipping)
    public double calculateTotal() {
        // TODO: 計算最終總金額 (Subtotal - Discount + Shipping)
        return 0.0;
    }

    // 結帳流程，呼叫付款處理邏輯，成功後變更狀態為 Completed
    public boolean checkout(IPaymentProcessor payment) {
        // TODO: 呼叫付款處理邏輯，成功後變更狀態為 Completed
        return false;
    }

    // 終端：印出訂單明細
    public void printReceipt() {
        System.out.println("====== 訂單明細 ======");
        System.out.println("訂單編號: " + orderId);
        System.out.println("買家: " + buyer.getName());
        System.out.println("----------------------");
        double subtotal = 0;
        for (CartItem item : items) {
            System.out.println(item.getProduct().getName() + " x " + item.getQuantity() + " = $" + item.getItemSubtotal());
            subtotal += item.getItemSubtotal();
        }
        System.out.println("----------------------");
        System.out.println("小計: $" + subtotal);

        double discount = discountStrategy != null ? discountStrategy.calculateDiscount(subtotal) : 0;
        double shipping = shippingStrategy != null ? shippingStrategy.calculateShipping(subtotal - discount) : 0;

        System.out.println("折扣: -$" + discount);
        System.out.println("運費: $" + shipping);
        System.out.println("總計: $" + calculateTotal());
        System.out.println("======================");
    }

    // Getters and Setters
    public String getOrderId() { return orderId; }
    public User getBuyer() { return buyer; }
    public List<CartItem> getItems() { return items; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
