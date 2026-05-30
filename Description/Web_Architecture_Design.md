# E-commerce Shopping Platform - 前後端分離架構設計 (Web Architecture)

## 一、 系統架構總覽 (System Architecture)
本系統採用**前後端分離 (Frontend-Backend Separation)**，透過 RESTful API 以 JSON 格式交換資料。
*   **前端 (Frontend)**: HTML + CSS + JavaScript (採用原生 Fetch API 串接)。負責畫面渲染、購物車與後台管理介面。
*   **後端 (Backend)**: Java (使用內建 `HttpServer` 建立的 `SimpleWebServer`)。負責處理核心邏輯，如登入驗證、購物車計算、結帳及商品管理。

---

## 二、 後端伺服器設計 (Backend: Java SimpleWebServer)
採用單一伺服器入口 `SimpleWebServer` 提供靜態檔案伺服器與 API 端點：

### 1. API 路由 (Contexts)
負責接收 HTTP 請求並導向對應的處理方法：
*   **`/api/products`**: 提供商品型錄查詢。
*   **`/api/login`**: 處理客戶與管理員登入驗證。
*   **`/api/cart/calculate`**: 即時計算購物車金額（含運費與折扣）。
*   **`/api/cart/checkout`**: 處理結帳與庫存扣除。
*   **`/api/products/sync`**: 管理員同步/更新商品資料。

### 2. 核心邏輯層 (Model 層)
*   **實體類別**: `Product`, `User`, `CartItem`, `ShoppingCart`, `Order` 等。
*   **策略模式 (Strategy Pattern)**: `IDiscountStrategy` (折扣策略) 與 `IShippingStrategy` (運費策略)。
*   **資料存取**: 透過 `CsvProductRepository` 讀寫 CSV 進行資料持久化。

---

## 三、 前端介面設計 (Frontend: HTML + JS)

### 單頁式應用 (SPA) - `index.html`
將所有功能整合於單一頁面，透過模態視窗 (Modal) 與面板切換顯示狀態：
*   **商品瀏覽與搜尋**: 支援即時搜尋與類別篩選，動態生成商品卡片。
*   **購物車模組**: 即時顯示所選商品清單，提供小計與總金額試算。
*   **結帳付款**: 透過模態視窗填寫付款資訊、套用折扣碼 (`SAVE10`, `FLAT50`) 及選擇運送方式。
*   **管理員後台**: 供 `admin` 帳號登入後使用，支援商品的新增、編輯、刪除同步，與客戶資訊查詢。

---

## 四、 API 互動規格 (RESTful API Endpoints)

*   **`GET /api/products`**: 獲取所有商品清單。
*   **`POST /api/login`**: 驗證帳號密碼並回傳權限狀態。
*   **`POST /api/cart/calculate`**: 傳送目前購物車內容、運送方式與折扣碼，回傳試算後的總金額、運費與折扣。
*   **`POST /api/cart/checkout`**: 傳送最終訂單資訊以完成結帳流程。
*   **`POST /api/products/sync`**: (限管理員) 傳送最新的商品清單 JSON 以覆寫與更新資料庫庫存。
