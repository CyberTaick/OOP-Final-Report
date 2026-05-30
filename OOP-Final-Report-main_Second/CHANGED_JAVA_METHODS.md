# Java 變更方法彙整

日期：2026-05-28

此檔彙整從專案開始到目前為止，在 Java 原始碼中我們已確認**新增或修改**的重要方法（以檔案為單位）。內容基於工作過程中所做的重構與修正。若要更精確的逐行 diff，請告訴我我會產生 git-style diff（若有 git 可用）。

## 檔案：[src/main/java/ecommerce/class_/User.java](src/main/java/ecommerce/class_/User.java)
- `User(String userId, String name, String role, String password)`：新增含 `role` 與 `password` 的建構子，取代原簡化建構子，可建立管理者或客戶帳號。
- `User(String userId, String name)`：保留並導向新建構子以保持相容性。
- `boolean authenticate(String password)`：加入密碼驗證邏輯。
- `boolean isAdmin()`：加入以判斷管理員身分的輔助方法。
- 各種 `get/set` 方法：新增/暴露 `role` 與 `password` 的 getter/setter。

## 檔案：[src/main/java/ecommerce/SimpleWebServer.java](src/main/java/ecommerce/SimpleWebServer.java)
以下方法為伺服器端新實作或重要修改：
- `public static void main(String[] args)`：啟動 `HttpServer`、註冊各路由（/api/products, /api/cart/*, /api/login, /api/products/sync, / ）。
- `private static void handleProducts(HttpExchange exchange)`：回傳商品清單 JSON（由 `ShoppingSystem` 取得），並組裝前端所需欄位（id,name,price,stock,description,category,images）。
- `private static void handleCartCalculate(HttpExchange exchange)`：處理購物車金額計算（折扣、運費、小計、總計）。
- `private static void handleCartCheckout(HttpExchange exchange)`：處理結帳流程（建立訂單、呼叫付款、檢查/扣庫存、寫回 CSV）。
- `private static void handleLogin(HttpExchange exchange)`：簡易帳密驗證，回傳使用者 id/name/role。
- `private static void handleSyncProducts(HttpExchange exchange)`：接收前端同步的商品陣列，轉為 `Product` 列表並寫入 CSV（更新 `ShoppingSystem` 內容）。
- `private static void handleStatic(HttpExchange exchange)`：靜態檔案服務（index.html, js, css 等）。
- `private static boolean writeProductsCsv(List<Product> products)`：將商品陣列寫回 `products.csv` 的實作。
- `private static ShoppingCart buildCartFromRequest(Map<String,Object> requestData)`：從前端請求重建 `ShoppingCart` 物件。
- `private static IDiscountStrategy createDiscountStrategy(String discountCode)`：折扣策略工廠邏輯。
- `private static IShippingStrategy createShippingStrategy(String shippingMethod)`：運費策略工廠邏輯。
- `private static IPaymentProcessor createPaymentProcessor(String paymentMethod, String paymentAccount)`：付款處理器建構。
- `private static Object parseJson(String text)` 與內部 `JsonParser` 類別：自訂 JSON 解析器（用以移除對外部 JSON lib 的依賴）。

## 備註 / 其他可能變更位置
- `ecommerce.core.ShoppingSystem`：在伺服器流程中被使用（建立訂單、查詢商品）。若需完整變更清單，可搜尋 `ShoppingSystem` 的具體修改。
- `ecommerce.class_.*`（例如 `Product`, `ShoppingCart`, `CartItem`, `Order`）: 在結帳與同步流程有被呼叫與互動，部分行為（如 `Product.reduceStock`）被伺服器端流程使用並驗證。若要我列出這些類別內的具體修改方法，請允許我掃描並匯出細目。

---
如果你想要：
- 我可以把上述每個方法加上檔案路徑與行號鏈結（更精準）。
- 若本專案有 git，我可以產生完整的 git diff 彙整；或我可以把目前所有 Java 檔案的變更摘要化為 CSV/JSON 格式供機器讀取。

要我接下來做哪一項？
