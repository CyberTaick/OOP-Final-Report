# E-commerce Shopping Platform - 前後端分離架構設計

## 一、 系統架構總覽
本系統採用**前後端分離的單頁式應用 (SPA)** 架構，前端負責畫面渲染與互動，後端處理商業邏輯，雙方透過 RESTful API (JSON格式) 交換資料。

---

## 二、 後端伺服器設計 (Backend: Java)
透過單一進入點 `SimpleWebServer` 提供靜態資源與 API 服務。

*   **核心實體 (Model)**: `Product`, `User`, `CartItem`, `ShoppingCart`, `OrderRecord` (訂單紀錄)。
*   **設計模式**: 
    *   **策略模式 (Strategy)**: 實作於 `IDiscountStrategy` (折扣) 與 `IShippingStrategy` (運費)。
    *   **多型與介面 (Polymorphism)**: `IPaymentProcessor` 針對不同付款方式 (信用卡、LinePay) 提供統一介面。
*   **資料持久化**: 透過 `CsvProductRepository` 讀寫 `products.csv` 進行商品與庫存同步。

---

## 三、 前端介面與功能設計 (Frontend: HTML + JS)
採用卡片式設計，所有操作皆於 `index.html` 內透過面板與模態視窗動態切換：

*   **帳號系統**: 
    *   顧客 (`customer`/`cust123`)：可瀏覽與購買。
    *   管理員 (`admin`/`admin123`)：登入後解鎖專屬後台面板。
*   **商品與購物車**:
    *   支援頂部文字搜尋與左側分類篩選。
    *   動態生成商品卡片（顯示名稱、價格、庫存與多圖），支援點擊查看詳情。
    *   購物車面板即時更新清單，自動計算小計。
*   **結帳付款模組**:
    *   可選擇固定運費或滿額免運。
    *   支援輸入折扣碼（如：`SAVE10`, `FLAT50`）。
    *   提供信用卡、Line Pay、ATM 等模擬付款介面。
*   **管理員後台功能**:
    *   **商品管理**: 提供完整的商品新增、編輯、刪除 (CRUD) 介面。
    *   **資料檢視**: 檢視現有客戶列表與可用的折扣活動清單。

---

## 四、 API 互動規格 (RESTful Endpoints)
前後端溝通合約，由前端透過 `Fetch API` 發送：

*   **`GET /api/products`**: 取得所有商品清單。
*   **`POST /api/login`**: 驗證帳號密碼，回傳身分權限。
*   **`POST /api/products/sync`**: (限管理員) 將編輯後的商品資料同步回寫至後端 CSV。

---

## 五、 系統運行與前後端互動流程
本系統運作依循「客戶端觸發 -> API 請求 -> 後端邏輯處理 -> 回傳更新」的完整閉環：

1. **伺服器啟動與初始化**
   * 執行 `SimpleWebServer` 後，伺服器監聽 `8080` 通訊埠。
   * 系統透過 `CsvProductRepository` 讀取 `products.csv`，將商品清單與庫存載入記憶體。

2. **前端載入與資源請求**
   * 使用者在瀏覽器輸入 `http://localhost:8080/`。
   * 後端攔截 `/` 路由，回傳 `index.html` 以及關聯的靜態資源 (`style.css`, `main.js`, `jsToJava.js`)。「當使用者打開網址時，Java 伺服器會自動把構成網頁畫面的所有檔案（包含 HTML、樣式表、腳本）打包發送給瀏覽器，讓畫面成功顯示出來。」

3. **使用者互動與 API 非同步呼叫 (Fetch)**
   * **瀏覽商品**：前端載入完成後，自動觸發 `GET /api/products`，後端回傳 JSON，前端解析後動態渲染商品卡片。
   * **登入驗證**：使用者輸入帳號密碼，前端發送 `POST /api/login`，後端驗證後回傳身分 (`customer` 或 `admin`)，前端據此解鎖特定 UI 面板。
   * **購物車試算**：每當購物車內容、運費選項或折扣碼變動，前端即時發送 `POST /api/cart/calculate`。後端套用 `IShippingStrategy` 與 `IDiscountStrategy` 進行精確運算並回傳各項明細，前端更新畫面。

4. **結帳與庫存扣除 (Checkout Flow)**
   * 使用者點擊結帳並選定付款方式 (`IPaymentProcessor`)。
   * 前端發送 `POST /api/cart/checkout` 包含最終訂單陣列。
   * 後端驗證庫存，建立 `OrderRecord` 紀錄，執行多型付款邏輯，並呼叫 `Product.reduceStock()` 扣除記憶體中的實體庫存，最後回傳成功訊息。

5. **後台同步與持久化 (Admin Sync)**
   * 管理員在前端後台進行商品的新增、修改或刪除。
   * 點擊「儲存」後，前端將更新後的商品清單打包發送 `POST /api/products/sync`。
   * 後端接收 JSON 並覆寫回本機的 `products.csv` 檔案，確保伺服器重啟後資料不遺失。
