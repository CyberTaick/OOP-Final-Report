package ecommerce.class_;

// 購物車中每個產品項的類別，包含產品和數量

public class CartItem {
    private Product product;
    private int quantity;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public double getItemSubtotal() {
        // 計算此項目的小計金額
        return product.getPrice() * quantity;
    }

    // Getters and Setters
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
