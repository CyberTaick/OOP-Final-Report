# OOP Final — SimpleWebServer (整合說明)

目前專案已改成使用純 Java 的 `SimpleWebServer` 來提供前端靜態頁面與後端 API，已移除 Maven/Spring Boot 相關入口。

## 啟動方式

1. 使用 Java 直接編譯並啟動：

2. 或使用你的 IDE 直接執行 `src/main/java/ecommerce/SimpleWebServer.java`。

3. 啟動後，開啟瀏覽器到：

```
http://localhost:8080/
```
## 登入帳密

管理員帳號密碼: admin / admin123

顧客帳號密碼:   customer / cust123  

## 折扣碼

支援折扣碼 `SAVE10` 與 `FLAT50`

## 靜態資源與 API

`web/jsToJava.js` 會對 `http://localhost:8080/api` 發出請求。ㄎ

## 已實作的 API

- `GET /api/products` — 取得商品清單
- `POST /api/cart/calculate` — 計算購物車金額（接收 `cartItems`, `discountCode`, `shippingMethod`）
- `POST /api/cart/checkout` — 模擬結帳
- `POST /api/products/sync` — 將前端商品陣列寫回 `products.csv`
- `POST /api/login` — 測試用簡易登入（admin/admin123, customer/cust123）
