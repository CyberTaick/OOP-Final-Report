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
        // TODO: 減少庫存
    }

    // 增加庫存
    public void increaseStock(int quantity) {
        // TODO: 增加庫存
    }

    // 回傳商品詳細資訊
    public String getDetails() {
        // TODO: 回傳商品詳細資訊
        return "";
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
