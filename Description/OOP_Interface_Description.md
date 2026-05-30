# 物件導向程式設計 (OOP) 核心特性與介面設計說明

本文件詳細說明了電子商務系統中，物件導向三大核心特性（封裝、抽象、多型）的實際應用位置與物件之間的交互行為。

---

## 一、 封裝 (Encapsulation)

**核心概念**：隱藏物件內部的狀態和實作細節，僅對外提供安全、可控的方法來進行存取，防止外部物件直接竄改內部資料。

*   **所在檔案**：`src/main/java/ecommerce/class_/Product.java`
*   **程式碼位置**：
    ```java
    public class Product {
        // 1. 內部狀態被隱藏 (private)
        private String productId;
        private double price;
        private int stockQuantity;

        // 2. 提供公開且具備邏輯驗證的方法對外互動
        public void reduceStock(int quantity) {
            if (quantity <= 0) return;
            if (quantity > stockQuantity) {
                stockQuantity = 0;
            } else {
                stockQuantity -= quantity;
            }
        }
    }
    ```
*   **與其他物件的交互行為**：
    當系統（如結帳流程中）需要扣除商品庫存時，外部物件**無法**直接執行 `product.stockQuantity = -10;` 來隨意篡改或破壞庫存數字，而是必須呼叫 `product.reduceStock(quantity)`。`Product` 物件會在內部自行檢查剩餘數量，確保庫存不會變成負數。這保障了資料的安全性與邏輯的一致性。
*   **相關檔案**：
    *   `src/main/java/ecommerce/class_/Product.java` — 商品的狀態和價格隱藏
    *   `src/main/java/ecommerce/class_/ShoppingCart.java` — 購物車項目列表隱藏
    *   `src/main/java/ecommerce/class_/CartItem.java` — 購物項目資料隱藏
    *   `src/main/java/ecommerce/class_/User.java` — 使用者的購物車隱藏
    *   `src/main/java/ecommerce/class_/OrderRecord.java` — 訂單狀態隱藏
    *   `src/main/java/ecommerce/class_/CreditCardPayment.java`, `LinePayPayment.java` — 付款帳號隱藏

---

## 二、 抽象 (Abstraction)

**核心概念**：抽出系統中共通的行為特徵定義成「介面 (Interface)」或「抽象類別」，而不提供具體的實作細節。讓上層邏輯只依賴這些介面，降低系統各部分的耦合度。

*   **所在檔案**：`src/main/java/ecommerce/interface_/IPaymentProcessor.java` 
    *(註：`IDiscountStrategy.java`, `IShippingStrategy.java` 也是極佳的抽象範例)*
*   **程式碼位置**：
    ```java
    package ecommerce.interface_;

    public interface IPaymentProcessor {
        // 僅定義行為合約，不包含具體實作
        boolean processPayment(double amount);
    }
    ```
*   **與其他物件的交互行為**：
    `IPaymentProcessor` 提供了一個付款的「標準合約」。這使得負責處理訂單的 `OrderRecord` 完全不需要知道「信用卡如何連線銀行」或「LINE Pay API 怎麼打」的底層細節。`OrderRecord` 只需要知道：只要有物件實作了這個介面，就可以把金額 (`amount`) 傳進去並得到是否付款成功 (`boolean`) 的結果。這實踐了依賴反轉，切斷了業務邏輯與第三方套件的直接牽連。
*   **相關檔案**：
    *   `src/main/java/ecommerce/interface_/IPaymentProcessor.java` — 付款處理器介面
    *   `src/main/java/ecommerce/interface_/IDiscountStrategy.java` — 折扣策略介面
    *   `src/main/java/ecommerce/interface_/IShippingStrategy.java` — 運費策略介面
    *   `src/main/java/ecommerce/interface_/IProductRepository.java` — 商品儲存庫介面
    *   `src/main/java/ecommerce/class_/OrderRecord.java` — 使用 IPaymentProcessor
    *   `src/main/java/ecommerce/SimpleWebServer.java` — 使用各個策略介面

---

## 三、 多型 (Polymorphism)

**核心概念**：同一個介面或父類別的方法，在執行時會根據傳入的「真實子類別實例 (Instance)」不同，而動態呈現出不同的行為。

*   **所在檔案**：
    *   **呼叫端**：`src/main/java/ecommerce/class_/OrderRecord.java`
    *   **實作端**：`src/main/java/ecommerce/class_/CreditCardPayment.java`, `LinePayPayment.java`
*   **程式碼位置**：
    ```java
    // 在 OrderRecord.java 中
    public boolean checkout(IPaymentProcessor payment) {
        if (payment == null) return false;
        double amount = calculateTotal();
        
        // 多型展現：程式在執行當下，才會決定這裡是執行信用卡邏輯還是 Line Pay 邏輯
        boolean paid = payment.processPayment(amount); 
        
        if (paid) this.status = "Completed";
        return paid;
    }
    ```
*   **與其他物件的交互行為**：
    `OrderRecord` 在執行 `checkout()` 時，期待收到一個宣告為 `IPaymentProcessor` 型態的物件。
    系統（或網頁端）可以根據使用者的選擇，動態傳入 `new CreditCardPayment("1234...")` 或是 `new LinePayPayment("user_id")`。
    
    當 `OrderRecord` 執行 `payment.processPayment(amount)` 時：
    1.  若傳入的是信用卡物件，Java 會動態綁定並觸發信用卡連線扣款的程式碼。
    2.  若傳入的是 LINE Pay 物件，Java 則會觸發向 LINE Pay 伺服器發送請求的程式碼。
    
    **交互設計的最大優勢**：未來如果要新增「Apple Pay」或「街口支付」，開發者只要撰寫新的類別來實作 `IPaymentProcessor` 即可。**完全不需要修改 `OrderRecord.java` 的任何程式碼**，這完美實踐了物件導向中的「開放封閉原則 (OCP)」。
*   **相關檔案**：
    *   **呼叫端（依賴介面）**：
        *   `src/main/java/ecommerce/class_/OrderRecord.java` — 呼叫 `IPaymentProcessor.processPayment()`
        *   `src/main/java/ecommerce/SimpleWebServer.java` — 呼叫 `IDiscountStrategy`, `IShippingStrategy`, `IPaymentProcessor`
        *   `src/main/java/ecommerce/core/ShoppingSystem.java` — 使用 `IProductRepository`
    *   **實作端（多種實現）**：
        *   `src/main/java/ecommerce/class_/CreditCardPayment.java` 實現 `IPaymentProcessor`
        *   `src/main/java/ecommerce/class_/LinePayPayment.java` 實現 `IPaymentProcessor`
        *   `src/main/java/ecommerce/class_/PercentageDiscount.java` 實現 `IDiscountStrategy`
        *   `src/main/java/ecommerce/class_/FlatRateShipping.java` 實現 `IShippingStrategy`
        *   `src/main/java/ecommerce/class_/FreeShippingThreshold.java` 實現 `IShippingStrategy`
        *   `src/main/java/ecommerce/data_access_object/CsvProductRepository.java` 實現 `IProductRepository`

---

## 四、 繼承 (Inheritance)

**核心概念**：透過「繼承」機制，子類別可以取得父類別或介面的所有特性（屬性和方法），並能在此基礎上進行擴展或覆寫。這促進了程式碼的重用，並建立了一套明確的類別層級結構。

在本系統中，採用「介面實現」的繼承模式，而非傳統的類別繼承 (extends)，這使得系統更加靈活與易於擴展。

*   **策略介面的實現繼承**：
    *   **所在檔案**：
        *   **父介面**：`src/main/java/ecommerce/interface_/IPaymentProcessor.java`
        *   **子實現**：`src/main/java/ecommerce/class_/CreditCardPayment.java`, `LinePayPayment.java`
    *   **程式碼位置**：
        ```java
        // 父介面定義
        public interface IPaymentProcessor {
            boolean processPayment(double amount);
        }
        
        // 子實現 1：信用卡付款
        public class CreditCardPayment implements IPaymentProcessor {
            @Override
            public boolean processPayment(double amount) {
                // 信用卡特定的付款邏輯
                System.out.println("💳 正在連線至信用卡公司授權扣款...");
                return true;
            }
        }
        
        // 子實現 2：LINE Pay 付款
        public class LinePayPayment implements IPaymentProcessor {
            @Override
            public boolean processPayment(double amount) {
                // LINE Pay 特定的付款邏輯
                System.out.println("📱 正在連線至 LINE Pay 伺服器...");
                return true;
            }
        }
        ```
    *   **與其他物件的交互行為**：
        `CreditCardPayment` 和 `LinePayPayment` 雖然實現方式不同，但都必須遵守 `IPaymentProcessor` 的契約。`OrderRecord` 或其他呼叫者不需要知道具體是哪一種付款方式，只要知道「所有實現 `IPaymentProcessor` 的物件都具有 `processPayment()` 方法」即可。

*   **折扣策略的實現繼承**：
    *   **所在檔案**：
        *   **父介面**：`src/main/java/ecommerce/interface_/IDiscountStrategy.java`
        *   **子實現**：`src/main/java/ecommerce/class_/PercentageDiscount.java`
    *   **說明**：百分比折扣實現了折扣策略介面，定義如何根據商品小計計算折扣金額。

*   **運費策略的實現繼承**：
    *   **所在檔案**：
        *   **父介面**：`src/main/java/ecommerce/interface_/IShippingStrategy.java`
        *   **子實現**：
            *   `src/main/java/ecommerce/class_/FlatRateShipping.java` — 固定運費
            *   `src/main/java/ecommerce/class_/FreeShippingThreshold.java` — 滿額免運
    *   **說明**：不同的運費計算方式都實現了統一的 `IShippingStrategy` 介面，`SimpleWebServer` 可根據使用者選擇動態切換。

*   **商品儲存庫的實現繼承**：
    *   **所在檔案**：
        *   **父介面**：`src/main/java/ecommerce/interface_/IProductRepository.java`
        *   **子實現**：`src/main/java/ecommerce/data_access_object/CsvProductRepository.java` — 從 CSV 檔案讀取商品
    *   **說明**：未來如果需要從資料庫或 API 讀取商品資料，只要實現同一個介面即可，不需要修改 `ShoppingSystem`。

*   **相關檔案**：
    *   **介面定義**（父）：`src/main/java/ecommerce/interface_/` 資料夾中的所有介面檔案
    *   **實現類別**（子）：`src/main/java/ecommerce/class_/` 和 `src/main/java/ecommerce/data_access_object/` 資料夾中的所有實現檔案
    *   **使用繼承的核心類別**：`src/main/java/ecommerce/core/ShoppingSystem.java`, `src/main/java/ecommerce/SimpleWebServer.java`
