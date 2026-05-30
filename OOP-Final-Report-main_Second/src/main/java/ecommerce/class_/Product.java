package ecommerce.class_;

public class Product {
    private String productId;
    private String name;
    private double price;
    private int stockQuantity;

    // 建構子
    public Product(String productId, String name, double price, int stockQuantity) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    // 減少庫存
    public void reduceStock(int quantity) {
        if (quantity <= 0) {
            return;
        }
        if (quantity > stockQuantity) {
            stockQuantity = 0;
        } else {
            stockQuantity -= quantity;
        }
    }

    // 增加庫存
    public void increaseStock(int quantity) {
        if (quantity > 0) {
            stockQuantity += quantity;
        }
    }

    // 回傳商品詳細資訊
    public String getDetails() {
        return String.format("商品：%s，價格：%.2f，庫存：%d", name, price, stockQuantity);
    }

    // Getters and Setters
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }
}
