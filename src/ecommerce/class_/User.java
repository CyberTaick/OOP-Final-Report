package ecommerce.class_;

public class User {
    // 使用者資訊
    private String userId;
    private String name;
    // 購物車：final 確保每個用戶只有一個購物車實例
    private final ShoppingCart cart;
    
    // 建構子
    public User(String userId, String name) {
        this.userId = userId;
        this.name = name;
        this.cart = new ShoppingCart();
    }

    // 取得用戶個人資訊
    public String getProfile() {
        // TODO: 取得用戶個人資訊
        return "User: " + name + " (ID: " + userId + ")";
    }

    // Getters and Setters
    // 外部物件 的 互動介面
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public ShoppingCart getCart() { return cart; }
}
