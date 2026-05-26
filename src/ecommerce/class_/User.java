package ecommerce.class_;

public class User {
    private String userId;
    private String name;
    private final ShoppingCart cart;

    public User(String userId, String name) {
        this.userId = userId;
        this.name = name;
        this.cart = new ShoppingCart();
    }

    public String getProfile() {
        // TODO: 取得用戶個人資訊
        return "User: " + name + " (ID: " + userId + ")";
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public ShoppingCart getCart() { return cart; }
}
