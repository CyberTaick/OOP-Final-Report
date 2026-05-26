# E-commerce Shopping Platform - 系統設計文件

## 一、 系統實作流程 (Implementation Workflow)

### 階段一：需求分析與核心功能定義
1. **商品管理**：提供商品清單，展示商品名稱、價格、庫存等資訊。
2. **用戶系統**：用戶可以瀏覽商品並擁有專屬的購物車。
3. **購物車機制**：支援將商品加入購物車、修改數量、移除商品。
4. **結帳與訂單生成**：計算最終總金額（包含商品原價、折扣扣抵、運費），並生成最終訂單。

### 階段二：物件導向架構設計 (Class Design)
定義核心的實體 (Entities) 與介面 (Interfaces)，包含 Product, User, ShoppingCart, Order 等類別，並設計計算折扣與運費的策略介面 (Strategy Pattern)。

### 階段三：實作核心類別與邏輯
1. **建置基礎資料模型**：Product, User, CartItem。
2. **實作購物車邏輯**：ShoppingCart (加入/移除商品、計算小計、佔用庫存)。
3. **實作計費與訂單邏輯**：實作運費與折扣策略，整合至 Order 類別。
4. **系統整合**：建立 ShoppingSystem 總管所有商品與用戶。

### 階段四：使用者互動介面設計 (CLI / Console)
實作主控台選單：瀏覽商品、加入購物車、檢視購物車、結帳。

### 階段五：異常處理與邊界測試 (Edge Cases)
1. 庫存不足防呆。
2. 空購物車禁止結帳。
3. 負數數量輸入驗證。

---

## 二、 類別與介面設計 (Class & Interface Architecture)

### 1. 介面設計 (Interfaces)
為了讓系統具備擴充性 (例如未來有不同的打折方式或運送方式)，我們採用策略模式 (Strategy Pattern)：

*   **IDiscountStrategy (折扣策略介面)**
    *   double CalculateDiscount(double subtotal)：根據小計計算折扣金額。
*   **IShippingStrategy (運費策略介面)**
    *   double CalculateShipping(double subtotal)：根據小計與條件計算運費。
*   **IPaymentProcessor (支付介面)**
    *   ool ProcessPayment(double amount)：處理付款邏輯。

### 2. 核心實體類別 (Core Classes)

*   **Product (商品)**
    *   **屬性**：ProductID, Name, Price, StockQuantity
    *   **方法**：ReduceStock(int quantity), IncreaseStock(int quantity), GetDetails()
*   **CartItem (購物車項目)**
    *   **屬性**：Product, Quantity
    *   **方法**：GetItemSubtotal()
*   **User (用戶)**
    *   **屬性**：UserID, Name, Cart (為 ShoppingCart 實例)
    *   **方法**：GetProfile()
*   **ShoppingCart (購物車)**
    *   **屬性**：Items (List of CartItem)
    *   **方法**：
        *   AddItem(Product p, int quantity)
        *   RemoveItem(ProductID id)
        *   GetSubtotal()
        *   Clear()
*   **Order (訂單)**
    *   **屬性**：OrderID, Buyer (User), Items, DiscountStrategy, ShippingStrategy, Status (Pending/Completed)
    *   **方法**：
        *   CalculateTotal(): return Subtotal - Discount + Shipping
        *   Checkout(IPaymentProcessor payment): 呼叫金流，成功後更動 Status 
        *   PrintReceipt(): 印出訂單明細

### 3. 主控制與管理類別 (System / Manager Classes)

*   **ShoppingSystem (平台管理系統 - Facade)**
    *   **屬性**：Products (商品庫), Users (用戶庫)
    *   **方法**：
        *   DisplayProducts()
        *   RegisterUser(name)
        *   CreateOrder(User user)

### 4. 具體策略實作 (Concrete Strategies)
*   **PercentageDiscount** (實作 IDiscountStrategy)：依比例打折 (例如全館9折)。
*   **FlatRateShipping** (實作 IShippingStrategy)：固定運費。
*   **FreeShippingThreshold** (實作 IShippingStrategy)：滿額免運。
