package ecommerce;

import ecommerce.class_.*;
import ecommerce.core.ShoppingSystem;
import ecommerce.data_access_object.CsvProductRepository;
import ecommerce.interface_.*;
import java.util.Scanner;

/**
 * 程式進入點
 * 提供控制台互動介面，供使用者操作購物系統（如瀏覽商品、加入購物車、結帳等）。
 */
public class Main {
    public static void main(String[] args) {
        // 設定資料來源為 CSV 檔案
        IProductRepository productRepo = new CsvProductRepository("products.csv");
        ShoppingSystem system = new ShoppingSystem(productRepo);

        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("歡迎來到 E-Commerce 平台！");
            System.out.print("請輸入您的姓名以註冊會員: ");
            String userName = scanner.nextLine();
            User currentUser = system.registerUser(userName);

            boolean running = true;
            while (running) {
                System.out.println("\n=== 🛍️ 主選單 ===");
                System.out.println("1. 瀏覽商品");
                System.out.println("2. 加入購物車");
                System.out.println("3. 檢視購物車");
                System.out.println("4. 結帳");
                System.out.println("5. 離開系統");
                System.out.print("👉 請選擇操作 (1-5): ");
                
                String choice = scanner.nextLine();
                switch (choice) {
                    case "1" -> {
                        System.out.println("\n--- 📦 商品清單 ---");
                        system.displayProducts();
                    }
                    case "2" -> {
                        System.out.print("請輸入要購買的商品 ID: ");
                        String pid = scanner.nextLine();
                        Product selectedProduct = null;
                        for (Product p : system.getProducts()) {
                            if (p.getProductId().equals(pid)) {
                                selectedProduct = p;
                                break;
                            }
                        }
                        if (selectedProduct == null) {
                            System.out.println("❌ 找不到此商品！");
                            break;
                        }
                        System.out.print("請輸入數量: ");
                        try {
                            int qty = Integer.parseInt(scanner.nextLine());
                            currentUser.getCart().addItem(selectedProduct, qty);
                            System.out.println("✅ 已將 " + qty + " 個 " + selectedProduct.getName() + " 加入購物車！");
                        } catch (NumberFormatException e) {
                            System.out.println("❌ 錯誤：請輸入有效的數字數量");
                        } catch (Exception e) {
                            System.out.println("❌ 錯誤：" + e.getMessage());
                        }
                    }
                    case "3" -> {
                        System.out.println("\n--- 🛒 您的購物車 ---");
                        if (currentUser.getCart().getItems().isEmpty()) {
                            System.out.println("購物車目前是空的。");
                        } else {
                            for (CartItem item : currentUser.getCart().getItems()) {
                                System.out.println("- " + item.getProduct().getName() + " x" + item.getQuantity() + " = $" + item.getItemSubtotal());
                            }
                            System.out.println("✨ 目前小計: $" + currentUser.getCart().getSubtotal());
                        }
                    }
                    case "4" -> {
                        if (currentUser.getCart().getItems().isEmpty()) {
                            System.out.println("❌ 購物車是空的，無法結帳。");
                            break;
                        }
                        // 設定策略：滿 $3000 免運 (未滿運費 $60)，全館 9 折優惠
                        IShippingStrategy shipping = new FreeShippingThreshold(3000, 60);
                        IDiscountStrategy discount = new PercentageDiscount(0.1); // 10% off
                        
                        // 建立訂單
                        Order order = system.createOrder(currentUser, discount, shipping);
                        
                        System.out.println("\n準備結帳...");
                        order.printReceipt();
                        
                        System.out.print("是否確認付款？(Y/N): ");
                        if (scanner.nextLine().equalsIgnoreCase("y")) {
                            // 注入金流策略 (模擬使用信用卡付款)
                            IPaymentProcessor payment = new CreditCardPayment("4311-8888-8888-1111");
                            if (order.checkout(payment)) {
                                currentUser.getCart().clear();
                                System.out.println("🎉 訂單已完成！感謝您的購買。");
                            }
                        } else {
                            System.out.println("已取消結帳。");
                        }
                    }
                    case "5" -> {
                        running = false;
                        System.out.println("👋 感謝您的使用，再見！");
                    }
                    default -> System.out.println("❌ 無效的選項，請重新輸入。");
                }
            }
        }
    }
}