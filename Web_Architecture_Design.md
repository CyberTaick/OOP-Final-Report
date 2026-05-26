# E-commerce Shopping Platform - 前後端分離架構設計 (Web Architecture)

## 一、 系統架構總覽 (System Architecture)
本系統採用**前後端分離 (Frontend-Backend Separation)** 架構設計，將「使用者介面」與「業務邏輯」徹底拆分，兩者透過 RESTful API 以 JSON 格式進行資料交換。

*   **前端 (Frontend)**: HTML + CSS + JavaScript (可搭配原生的 Fetch API 或 Axios 等工具)。負責渲染畫面、展示商品與接收使用者的點擊操作。
*   **後端 (Backend)**: Java (建議使用 Spring Boot 框架)。負責處理核心功能，包含購物車運算、套用折扣設計模式 (Strategy Pattern)、庫存管理等。

---

## 二、 後端伺服器設計 (Backend: Java Spring Boot)
後端會延續原本寫在 `Design.md` 的核心類別，並加上 Controller 層來處理網路請求。

### 1. 控制層 (Controller 層 - API 端點)
負責接收來自 HTML/JS 的 HTTP 請求，驗證參數並指派給底下的服務層處理。
*   **`ProductController`**: 提供商品型錄查詢。
*   **`CartController`**: 控制購物車內容增刪改查。
*   **`OrderController`**: 負責最終結帳的動作。

### 2. 核心邏輯層 (Model 層 - 純 Java 物件導向核心)
將先前的設計直接應用在這裡：
*   **實體類別**: `Product`, `User`, `CartItem`, `ShoppingCart`, `Order`
*   **策略介面與實作 (Strategy Pattern)**:
    *   `IDiscountStrategy` (例如實作 `PercentageDiscount` 全館八折)
    *   `IShippingStrategy` (例如實作 `FreeShippingThreshold` 滿千免運)

---

## 三、 前端介面設計 (Frontend: HTML + JS)

### 1. 頁面設計 (Pages)
*   **`index.html` (商品瀏覽首頁)**
    *   動態呼叫後端 API 取得商品清單，利用 JavaScript (DOM 操作) 自動生成商品卡片。
    *   每個商品配有一個「Add to Cart (加到購物車)」按鈕。
*   **`cart.html` (購物車與結帳頁)**
    *   顯示清單：列出使用者目前所選的 `CartItem`，以及初步計算的小計 (Subtotal)。
    *   結帳按鈕：點擊後送出結帳請求，並顯示包含「運費」與「折扣」的最終訂單資訊。

---

## 四、 API 互動介面規格 (RESTful API Endpoints)
前後端溝通的合約 (Contract)，規定了網頁按鈕點下後，需呼叫哪個網址：

### [商品相關]
*   **`GET /api/products`**
    *   作用：獲取所有可用商品資訊。
    *   回傳範例：`[{"productId": 1, "name": "Laptop", "price": 30000, "stock": 10}, ...]`

### [購物車相關]
*   **`GET /api/cart/{userId}`**
    *   作用：載入目前的購物車狀態。
*   **`POST /api/cart/add`**
    *   作用：點擊「加入購物車」時觸發。
    *   送出資料：`{"userId": "U01", "productId": 1, "quantity": 1}`
*   **`DELETE /api/cart/remove/{productId}`**
    *   作用：從購物車中移除某項商品。

### [訂單與結帳相關]
*   **`POST /api/orders/checkout`**
    *   作用：點擊結帳時觸發。後端收到請求後，會套用對應的 `IDiscountStrategy` 和 `IShippingStrategy` 計算最終價格並扣除庫存。
    *   回傳範例：`{"orderId": "ORD-1001", "totalCost": 29000, "status": "Success"}`
