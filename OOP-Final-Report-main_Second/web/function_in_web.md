# 前端功能說明

商品卡、類別、購物車、管理員功能已大致完善
javascript還沒連上java

管理員帳號密碼: admin / admin123
顧客帳號密碼:   customer / cust123  

## 1. 頁面結構與版面
- 左側顯示商品類別清單
- 中間顯示商品卡片列表
- 右側顯示購物車內容
- 右上角提供「登入」按鈕，點擊會彈出登入模態視窗
- 管理者登入後會顯示額外管理者系統面板

## 2. 搜尋功能
- 頁首搜尋欄提供即時文字搜尋
- 使用者可以輸入商品名稱進行商品篩選
- 搜尋結果會動態更新商品列表

## 3. 登入系統
- 提供客戶與管理員登入選項
- 登入按鈕觸發登入模態視窗
- 登入成功後顯示使用者名稱與登出按鈕
- 管理員登入後顯示 `管理者系統` 面板

## 4. 商品卡片顯示
- 每個商品卡片顯示名稱、價格、分類、庫存
- 商品卡片可顯示多張圖片
- 若商品只有一張圖或無圖片，則只顯示一張圖片
- 點擊商品卡片會開啟商品詳細資訊模態視窗
- 詳情模態包含商品介紹、圖片、價格、分類、庫存、加入購物車按鈕

## 5. 購物車功能
- 購物車面板顯示加入的商品清單
- 每個商品可移除
- 顯示小計、運費、折扣、總計
- 「前往結帳」按鈕開啟付款模態視窗

## 6. 結帳與付款
- 結帳模態讓使用者選擇付款方式（信用卡、Line Pay、ATM）
- 可輸入收件姓名與卡號/帳號資訊
- 可選擇運送方式
- 可輸入折扣碼並套用折扣
- 確認付款後模擬付款流程並顯示結果

## 7. 運費與折扣
- 提供固定運費與滿額免運兩種運送規則
- 支援折扣碼 `SAVE10` 與 `FLAT50`
- 折扣可在結帳模態中套用

## 8. 管理者系統功能
- 管理者登入後會看到 `商品列表`、`客戶列表`、`折扣活動` 分頁
- `商品列表` 支援商品新增、編輯、刪除
- 新增/編輯表單包含商品名稱、類別、價格、庫存、描述、圖片網址
- 客戶列表顯示客戶帳號資訊
- 折扣活動列表顯示可用的折扣碼與折扣內容

## 9. 其他
- 介面採用卡片式設計與鮮明色彩
- 使用者登入狀態會顯示於頁面右上角
- 管理者面板僅管理員可見
- 商品分類會同步新增新的分類項目

## 10. 前端功能所需輸入資料
### 10.1 商品資料 (products)
- `id`: 數字，商品唯一識別碼
- `name`: 字串，商品名稱
- `price`: 數字，商品價格
- `category`: 字串，商品分類
- `stock`: 數字，庫存數量
- `description`: 字串，商品詳細描述
- `images`: 字串陣列，商品圖片網址

範例：
```js
const products = [
  {
    id: 1,
    name: '無線滑鼠',
    price: 399,
    category: '周邊',
    stock: 12,
    description: '高精度無線滑鼠，具備可自訂按鍵與人體工學設計。',
    images: ['https://...1', 'https://...2']
  }
];
```

### 10.2 類別資料 (categories)
- `categories`: 字串陣列，用於商品分類過濾

範例：
```js
const categories = ['所有', '周邊', '鍵盤', '充電'];
```

### 10.3 運費設定 (shippingOptions)
- `id`: 字串，運費方案識別碼
- `name`: 字串，顯示名稱
- `calc`: 函式，根據小計計算運費

範例：
```js
const shippingOptions = [
  {id:'flat', name:'固定運費 ($60)', calc:(subtotal)=>60},
  {id:'freeOver', name:'滿額免運 ($1000)', calc:(subtotal)=> subtotal>=1000?0:80}
];
```

### 10.4 折扣資料 (discounts)
- 折扣碼為物件鍵
- 每個折扣紀錄包含 `type` 與 `value`
- `type`: `percent` 或 `flat`
- `value`: 數字，百分比折扣或固定金額折扣

範例：
```js
const discounts = {
  'SAVE10': {type:'percent', value:10},
  'FLAT50': {type:'flat', value:50}
};
```

### 10.5 使用者帳號資料 (accounts)
- `username`: 登入帳號
- `password`: 密碼
- `role`: 身分 `管理員` 或 `客戶`

範例：
```js
const accounts = {
  admin: {username:'admin', password:'admin123', role:'管理員'},
  customer: {username:'customer', password:'cust123', role:'客戶'}
};
```

### 10.6 前端狀態資料 (state)
- `cart`: 購物車項目陣列，每項目包含 `id`, `name`, `price`, `qty`
- `shipping`: 目前選擇的運送方案 `id`
- `discountCode`: 當前套用的折扣碼
- `category`: 目前選擇的分類
- `search`: 搜尋關鍵字
- `user`: 登入使用者資訊，包含 `name` 與 `role`
- `currentProductId`: 目前查看的商品 ID
- `adminEditingId`: 管理者編輯商品時的商品 ID

範例：
```js
const state = {
  cart: [],
  shipping: 'flat',
  discountCode: null,
  category: '所有',
  search: '',
  user: null,
  currentProductId: null,
  adminEditingId: null
};
```
