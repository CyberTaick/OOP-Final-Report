package ecommerce.class_;

public class User {
    private String userId;
    private String name;
    private String role;
    private String password;
    private final ShoppingCart cart;

    public User(String userId, String name) {
        this(userId, name, "customer", "");
    }

    public User(String userId, String name, String role, String password) {
        this.userId = userId;
        this.name = name;
        this.role = role != null ? role : "customer";
        this.password = password != null ? password : "";
        this.cart = new ShoppingCart();
    }

    public String getProfile() {
        return String.format("User: %s (ID: %s, Role: %s)", name, userId, role);
    }

    public boolean authenticate(String password) {
        return password != null && password.equals(this.password);
    }

    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(role) || "管理員".equalsIgnoreCase(role);
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public ShoppingCart getCart() { return cart; }
}
