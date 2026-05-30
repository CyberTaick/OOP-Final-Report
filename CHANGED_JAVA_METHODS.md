# Java 變更方法彙整 (2026-05-28)

此文件彙整專案至今重要的新增與修改方法。

## 1. `ecommerce.class_.User`
主要擴充使用者屬性與驗證功能：
- **`User(String, String, String, String)`**: 新增支援 `role` (角色) 與 `password` (密碼) 的建構子。
- **`User(String, String)`**: 舊版建構子，保留相容性。
- **`authenticate(String)`**: 新增密碼驗證邏輯。
- **`isAdmin()`**: 判斷是否為管理員。
- **新增 Getter/Setter**: 針對 `role` 與 `password`。

## 2. `ecommerce.SimpleWebServer`
新增多項 HTTP 路由處理與輔助方法：
- **`main(String[])`**: 初始化 HttpServer 並註冊各 API 路由。
- **路由處理 (HTTP Handlers)**:
  - `handleProducts()`: 回傳所有商品的 JSON 列表。
  - `handleCartCalculate()`: 計算購物車金額 (含折扣、運費)。
  - `handleCartCheckout()`: 處理結帳 (建立訂單、扣庫存、更新 CSV)。
  - `handleLogin()`: 處理登入驗證。
  - `handleSyncProducts()`: 接收並同步前端商品資料至 CSV。
  - `handleStatic()`: 提供前端靜態檔案 (HTML/JS/CSS)。
- **輔助方法**:
  - `writeProductsCsv()`: 將商品資料覆寫至 `products.csv`。
  - `buildCartFromRequest()`: 解析請求並建立 `ShoppingCart`。
  - `createDiscountStrategy()` / `createShippingStrategy()` / `createPaymentProcessor()`: 各類策略與處理器的工廠方法。
  - `JsonParser (內部類別)`: 輕量級自訂 JSON 解析器。

## 3. 其他關聯變更
- **`ecommerce.core.ShoppingSystem`**: 調整以配合伺服器結帳與商品同步流程。
- **實體類別 (`Product`, `Order`, 等)**: 配合加入 `reduceStock` 等庫存操作邏輯。
