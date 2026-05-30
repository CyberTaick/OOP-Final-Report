# OOP Final — SimpleWebServer (整合說明)

目前專案已改成使用純 Java 的 `SimpleWebServer` 來提供前端靜態頁面與後端 API，已移除 Maven/Spring Boot 相關入口。

## 是否需要 `target` 目錄？

`target` 是 Maven 打包或編譯時產生的輸出目錄。既然專案現在改為用 `SimpleWebServer` 直接啟動，`target` 不是必要檔案，並且可以刪除。

## 啟動方式

1. 使用 Java 直接編譯並啟動：

```powershell
cd "OOP-Final-Report-main_Second"
javac -d out src\main\java\ecommerce\*.java src\main\java\ecommerce\class_\*.java src\main\java\ecommerce\core\*.java src\main\java\ecommerce\data_access_object\*.java src\main\java\ecommerce\interface_\*.java
java -cp out ecommerce.SimpleWebServer
```

2. 或使用你的 IDE 直接執行 `src/main/java/ecommerce/SimpleWebServer.java`。

3. 啟動後，開啟瀏覽器到：

```text
http://localhost:8080/
```

## 靜態資源與 API

`web/jsToJava.js` 會對 `http://localhost:8080/api` 發出請求。

請確保執行目錄包含 `products.csv`，格式範例：

```
1,無線滑鼠,399,12
2,機械鍵盤,1990,5
3,USB-C 充電器,350,20
```

## 已實作的 API

- `GET /api/products` — 取得商品清單
- `POST /api/cart/calculate` — 計算購物車金額（接收 `cartItems`, `discountCode`, `shippingMethod`）
- `POST /api/cart/checkout` — 模擬結帳
- `POST /api/products/sync` — 將前端商品陣列寫回 `products.csv`
- `POST /api/login` — 測試用簡易登入（admin/admin123, customer/cust123）

如果要我幫你測試啟動或撰寫 PowerShell 測試請求，告訴我，我可以接著處理。 
