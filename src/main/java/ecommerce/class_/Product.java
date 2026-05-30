package ecommerce.class_;

public class Product {
    private String productId;
    private String name;
    private double price;
    private int stockQuantity;
    private String category;

    // 建構子
    public Product(String productId, String name, double price, int stockQuantity, String category) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.category = (category == null || category.trim().isEmpty()) ? "一般" : category;
    }

    // 相容舊版的建構子 (如果呼叫時沒有傳入分類)
    public Product(String productId, String name, double price, int stockQuantity) {
        this(productId, name, price, stockQuantity, "一般");
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
        return String.format("商品：%s，分類：%s，價格：%.2f，庫存：%d", name, category, price, stockQuantity);
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

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
