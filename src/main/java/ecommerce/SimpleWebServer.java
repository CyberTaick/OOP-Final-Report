package ecommerce;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import ecommerce.class_.CartItem;
import ecommerce.class_.CreditCardPayment;
import ecommerce.class_.LinePayPayment;
import ecommerce.class_.OrderRecord;
import ecommerce.class_.Product;
import ecommerce.class_.ShoppingCart;
import ecommerce.class_.User;
import ecommerce.core.ShoppingSystem;
import ecommerce.data_access_object.CsvProductRepository;
import ecommerce.interface_.IDiscountStrategy;
import ecommerce.interface_.IPaymentProcessor;
import ecommerce.interface_.IShippingStrategy;
import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 處理前後端 API 請求與靜態資源的簡易 HTTP 伺服器主程式
public class SimpleWebServer {
    private static final String CSV_FILE = "src/main/java/ecommerce/data_access_object/products.csv";
    private static final String ORDERS_CSV_FILE = "src/main/java/ecommerce/data_access_object/orders.csv"; // 紀錄購買紀錄的 CSV 檔案路徑
    private static final ShoppingSystem system = new ShoppingSystem(new CsvProductRepository(CSV_FILE));

    // 程式進入點，初始化伺服器並註冊所有 API Router
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/api/products", SimpleWebServer::handleProducts);
        server.createContext("/api/cart/calculate", SimpleWebServer::handleCartCalculate);
        server.createContext("/api/cart/checkout", SimpleWebServer::handleCartCheckout);
        server.createContext("/api/login", SimpleWebServer::handleLogin);
        server.createContext("/api/products/sync", SimpleWebServer::handleSyncProducts);
        server.createContext("/", SimpleWebServer::handleStatic);

        server.setExecutor(null);
        System.out.println("🚀 SimpleWebServer 已啟動，請開啟 http://localhost:8080/");
        server.start();

        // Automatically open the default browser (Edge if it's the system default)
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI("http://localhost:8080/"));
            }
        } catch (IOException | URISyntaxException e) {
            System.err.println("自動開啟瀏覽器失敗：" + e.getMessage());
        }
    }

    // 處理 GET /api/products 請求，回傳所有商品清單的 JSON
    private static void handleProducts(HttpExchange exchange) throws IOException {
        addCors(exchange);
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendNoContent(exchange);
            return;
        }
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendMethodNotAllowed(exchange);
            return;
        }

        StringBuilder body = new StringBuilder("[");
        List<Product> products = system.getProducts();
        for (int i = 0; i < products.size(); i++) {
            Product p = products.get(i);
            body.append("{");
            body.append("\"id\":\"").append(escapeJson(p.getProductId())).append("\"");
            body.append(",\"name\":\"").append(escapeJson(p.getName())).append("\"");
            body.append(",\"price\":").append(p.getPrice());
            body.append(",\"stock\":").append(p.getStockQuantity());
            body.append(",\"active\":true");
            body.append(",\"description\":\"\"");
            body.append(",\"category\":\"").append(escapeJson(p.getCategory())).append("\"");
            body.append(",\"images\":[\"https://via.placeholder.com/300?text=").append(escapeJson(p.getName())).append("\"]");
            body.append("}");
            if (i < products.size() - 1) {
                body.append(",");
            }
        }
        body.append("]");
        sendJson(exchange, 200, body.toString());
    }

    // 處理 POST /api/cart/calculate 請求，計算購物車的折扣與總金額
    @SuppressWarnings("unchecked")
    private static void handleCartCalculate(HttpExchange exchange) throws IOException {
        addCors(exchange);
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendNoContent(exchange);
            return;
        }
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendMethodNotAllowed(exchange);
            return;
        }

        Object requestBody = parseJson(readRequestBody(exchange));
        if (!(requestBody instanceof Map)) {
            sendJson(exchange, 400, "{\"message\":\"無效的請求格式\"}");
            return;
        }
        Map<String, Object> requestData = (Map<String, Object>) requestBody;
        Object discountCodeObj = requestData.get("discountCode");
        Object shippingMethodObj = requestData.get("shippingMethod");
        String discountCode = discountCodeObj != null ? String.valueOf(discountCodeObj) : null;
        String shippingMethod = shippingMethodObj != null ? String.valueOf(shippingMethodObj) : "flat";

        ShoppingCart cart = buildCartFromRequest(requestData);
        IDiscountStrategy discountStrategy = createDiscountStrategy(discountCode);
        IShippingStrategy shippingStrategy = createShippingStrategy(shippingMethod);
        OrderRecord order = new OrderRecord("CALC-" + System.currentTimeMillis(), null, cart.getItems(), discountStrategy, shippingStrategy);

        String json = String.format(
            "{\"subtotal\":%.2f,\"discount\":%.2f,\"shipping\":%.2f,\"total\":%.2f}",
            cart.getSubtotal(),
            discountStrategy != null ? discountStrategy.calculateDiscount(cart.getSubtotal()) : 0.0,
            shippingStrategy != null ? shippingStrategy.calculateShipping(cart.getSubtotal()) : 0.0,
            order.calculateTotal()
        );
        sendJson(exchange, 200, json);
    }

    // 處理 POST /api/cart/checkout 請求，執行結帳流程、扣庫存並更新 CSV
    @SuppressWarnings("unchecked")
    private static void handleCartCheckout(HttpExchange exchange) throws IOException {
        addCors(exchange);
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendNoContent(exchange);
            return;
        }
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendMethodNotAllowed(exchange);
            return;
        }

        Object requestBody = parseJson(readRequestBody(exchange));
        if (!(requestBody instanceof Map)) {
            sendJson(exchange, 400, "{\"message\":\"無效的請求格式\"}");
            return;
        }
        Map<String, Object> requestData = (Map<String, Object>) requestBody;
        ShoppingCart cart = buildCartFromRequest(requestData);
        if (cart.getItems().isEmpty()) {
            sendJson(exchange, 400, "{\"success\":false,\"message\":\"購物車為空。\"}");
            return;
        }

        for (CartItem item : cart.getItems()) {
            Product product = system.findProductById(item.getProduct().getProductId());
            if (product == null) {
                sendJson(exchange, 400, String.format("{\"success\":false,\"message\":\"找不到商品: %s\"}", escapeJson(item.getProduct().getProductId())));
                return;
            }
            if (item.getQuantity() > product.getStockQuantity()) {
                sendJson(exchange, 400, String.format("{\"success\":false,\"message\":\"商品 %s 庫存不足。\"}", escapeJson(product.getName())));
                return;
            }
        }

        Object discountCodeObj = requestData.get("discountCode");
        Object shippingMethodObj = requestData.get("shippingMethod");
        Object paymentMethodObj = requestData.get("paymentMethod");
        Object paymentAccountObj = requestData.get("paymentAccount");
        String discountCode = discountCodeObj != null ? String.valueOf(discountCodeObj) : null;
        String shippingMethod = shippingMethodObj != null ? String.valueOf(shippingMethodObj) : "flat";
        String paymentMethod = paymentMethodObj != null ? String.valueOf(paymentMethodObj) : "card";
        String paymentAccount = paymentAccountObj != null ? String.valueOf(paymentAccountObj) : "0000";

        IDiscountStrategy discountStrategy = createDiscountStrategy(discountCode);
        IShippingStrategy shippingStrategy = createShippingStrategy(shippingMethod);
        User guest = new User("0", "guest");
        for (CartItem item : cart.getItems()) {
            guest.getCart().addItem(item.getProduct(), item.getQuantity());
        }

        OrderRecord order = system.createOrder(guest, discountStrategy, shippingStrategy);
        if (order == null) {
            sendJson(exchange, 500, "{\"success\":false,\"message\":\"訂單建立失敗。\"}");
            return;
        }

        IPaymentProcessor payment = createPaymentProcessor(paymentMethod, paymentAccount);
        boolean paid = order.checkout(payment);
        if (!paid) {
            sendJson(exchange, 400, "{\"success\":false,\"message\":\"付款失敗。請檢查付款資訊。\"}");
            return;
        }

        for (CartItem item : order.getItems()) {
            Product product = system.findProductById(item.getProduct().getProductId());
            if (product != null) {
                product.reduceStock(item.getQuantity());
            }
        }
        writeProductsCsv(system.getProducts());
        
        // 將購買紀錄寫入 orders.csv，供管理員查看
        appendOrderToCsv(order, paymentMethod);

        String response = String.format("{\"success\":true,\"orderId\":\"%s\",\"total\":%.2f}", order.getOrderId(), order.calculateTotal());
        sendJson(exchange, 200, response);
    }

    // 處理 POST /api/login 請求，驗證使用者帳號密碼並回傳角色資訊
    @SuppressWarnings("unchecked")
    private static void handleLogin(HttpExchange exchange) throws IOException {
        addCors(exchange);
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendNoContent(exchange);
            return;
        }
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendMethodNotAllowed(exchange);
            return;
        }

        Object requestBody = parseJson(readRequestBody(exchange));
        if (!(requestBody instanceof Map)) {
            sendJson(exchange, 400, "{\"message\":\"無效的請求格式\"}");
            return;
        }
        Map<String, Object> requestData = (Map<String, Object>) requestBody;
        String username = String.valueOf(requestData.getOrDefault("username", "")).trim();
        String password = String.valueOf(requestData.getOrDefault("password", ""));

        if ("admin".equalsIgnoreCase(username) && "admin123".equals(password)) {
            sendJson(exchange, 200, "{\"userId\":\"1\",\"name\":\"admin\",\"role\":\"管理員\"}");
            return;
        }
        if ("customer".equalsIgnoreCase(username) && "cust123".equals(password)) {
            sendJson(exchange, 200, "{\"userId\":\"2\",\"name\":\"customer\",\"role\":\"客戶\"}");
            return;
        }
        sendJson(exchange, 401, "{\"message\":\"帳號或密碼錯誤\"}");
    }

    // 處理 POST /api/products/sync 請求，接收前端傳來的商品資料並覆寫 CSV
    @SuppressWarnings("unchecked")
    private static void handleSyncProducts(HttpExchange exchange) throws IOException {
        addCors(exchange);
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendNoContent(exchange);
            return;
        }
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendMethodNotAllowed(exchange);
            return;
        }

        Object requestBody = parseJson(readRequestBody(exchange));
        if (!(requestBody instanceof List)) {
            sendJson(exchange, 400, "{\"message\":\"無效的請求格式\"}");
            return;
        }

        List<Product> newProducts = new ArrayList<>();
        for (Object item : (List<?>) requestBody) {
            if (!(item instanceof Map)) {
                continue;
            }
            Map<String, Object> productData = (Map<String, Object>) item;
            String id = String.valueOf(productData.getOrDefault("id", ""));
            String name = String.valueOf(productData.getOrDefault("name", ""));
            double price = toDouble(productData.getOrDefault("price", 0));
            int stock = toInt(productData.getOrDefault("stock", 0));
            String category = String.valueOf(productData.getOrDefault("category", "一般"));
            newProducts.add(new Product(id, name, price, stock, category));
        }

        system.getProducts().clear();
        system.getProducts().addAll(newProducts);
        boolean saved = writeProductsCsv(newProducts);
        if (!saved) {
            sendJson(exchange, 500, "{\"success\":false,\"message\":\"寫入 CSV 失敗\"}");
            return;
        }
        sendJson(exchange, 200, "{\"success\":true}");
    }

    // 處理網頁靜態資源請求，提供 HTML、CSS、JS 等前端檔案
    private static void handleStatic(HttpExchange exchange) throws IOException {
        addCors(exchange);
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendNoContent(exchange);
            return;
        }

        String path = exchange.getRequestURI().getPath();
        if ("/".equals(path) || "".equals(path)) {
            path = "/index.html";
        }

        Path filePath = Paths.get(".").resolve(path.substring(1)).normalize();
        if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
            sendNotFound(exchange);
            return;
        }

        String contentType = getContentType(filePath.toString());
        byte[] bytes = Files.readAllBytes(filePath);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    // 為 HTTP 回應加入 CORS 標頭，接受來自不同PORT的資源請求
    private static void addCors(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
    }

    // 處理 OPTIONS 預檢請求，回傳 HTTP 204 無內容回應
    private static void sendNoContent(HttpExchange exchange) throws IOException {
        try (exchange) {
            exchange.sendResponseHeaders(204, -1);
        }
    }

    // 回傳 HTTP 405 錯誤，表示不支援該 HTTP 方法
    private static void sendMethodNotAllowed(HttpExchange exchange) throws IOException {
        sendJson(exchange, 405, "{\"message\":\"方法不支援\"}");
    }

    // 回傳 HTTP 404 錯誤，表示找不到請求的資源
    private static void sendNotFound(HttpExchange exchange) throws IOException {
        sendJson(exchange, 404, "{\"message\":\"找不到資源\"}");
    }

    // 將 JSON 字串寫入 HTTP 回應主體中並結束請求
    private static void sendJson(HttpExchange exchange, int status, String json) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    // 從 HTTP 請求中讀取所有主體內容並轉為字串
    private static String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream in = exchange.getRequestBody()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    // 將目前的商品列表資料覆寫回 products.csv 檔案中
    private static boolean writeProductsCsv(List<Product> products) {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(CSV_FILE), StandardCharsets.UTF_8)) {
            for (Product p : products) {
                writer.write(String.join(",", 
                    escapeCsv(p.getProductId()), 
                    escapeCsv(p.getName()), 
                    String.valueOf(p.getPrice()), 
                    String.valueOf(p.getStockQuantity()),
                    escapeCsv(p.getCategory())
                ));
                writer.newLine();
            }
            writer.flush();
            return true;
        } catch (IOException e) {
            System.err.println("CSV 寫入失敗: " + e.getMessage());
            return false;
        }
    }

    // 將單筆結帳完成的訂單紀錄附加 (Append) 到 orders.csv 檔案的最後面
    private static void appendOrderToCsv(OrderRecord order, String paymentMethod) {
        Path path = Paths.get(ORDERS_CSV_FILE);
        
        // 確保目錄存在，若不存在則建立 (防止寫入失敗)
        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
        } catch (IOException e) {
            System.err.println("無法建立訂單目錄: " + e.getMessage());
        }

        boolean fileExists = Files.exists(path);
        
        // 開啟寫入器，設定為附加模式 (APPEND) 與建立模式 (CREATE)
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, 
                java.nio.file.StandardOpenOption.CREATE, 
                java.nio.file.StandardOpenOption.APPEND)) {
            
            // 如果檔案是新建立的，先寫入標題列 (Header)
            if (!fileExists) {
                writer.write("訂單編號,總金額,付款方式,購買商品明細");
                writer.newLine();
            }
            
            // 將購物車內的商品整理成一段明細字串 (例：機械鍵盤x1, 滑鼠x2)
            StringBuilder itemsDetail = new StringBuilder();
            for (CartItem item : order.getItems()) {
                itemsDetail.append(item.getProduct().getName())
                           .append("x")
                           .append(item.getQuantity())
                           .append("; ");
            }
            
            // 寫入這筆訂單的資料
            writer.write(String.join(",", 
                escapeCsv(order.getOrderId()), 
                String.format("%.2f", order.calculateTotal()), 
                escapeCsv(paymentMethod),
                escapeCsv(itemsDetail.toString())
            ));
            writer.newLine();
            writer.flush();
            System.out.println("📄 已將訂單紀錄儲存至 " + ORDERS_CSV_FILE);
        } catch (IOException e) {
            System.err.println("訂單紀錄寫入失敗: " + e.getMessage());
        }
    }

    // 前端傳來的請求資料，藉此重建 ShoppingCart 物件
    @SuppressWarnings("unchecked")
    private static ShoppingCart buildCartFromRequest(Map<String, Object> requestData) {
        ShoppingCart cart = new ShoppingCart();
        Object itemsObj = requestData.get("cartItems");
        if (!(itemsObj instanceof List)) {
            return cart;
        }
        for (Object itemObj : (List<?>) itemsObj) {
            if (!(itemObj instanceof Map)) {
                continue;
            }
            Map<String, Object> item = (Map<String, Object>) itemObj;
            String id = String.valueOf(item.getOrDefault("id", item.getOrDefault("productId", "")));
            int qty = toInt(item.getOrDefault("qty", item.getOrDefault("quantity", 0)));
            Product product = system.findProductById(id);
            if (product != null && qty > 0) {
                cart.addItem(product, qty);
            }
        }
        return cart;
    }

    // 前端傳入折扣碼，建立並回傳對應的DiscountStrategy
    private static IDiscountStrategy createDiscountStrategy(String discountCode) {
        if (discountCode == null) {
            return new ecommerce.class_.PercentageDiscount(0.0);
        }
        if ("SAVE10".equalsIgnoreCase(discountCode)) {
            return new ecommerce.class_.PercentageDiscount(0.10);
        }
        if ("FLAT50".equalsIgnoreCase(discountCode)) {
            return new ecommerce.class_.PercentageDiscount(0.0) {
                @Override
                public double calculateDiscount(double subtotal) {
                    return Math.min(50.0, subtotal);
                }
            };
        }
        return new ecommerce.class_.PercentageDiscount(0.0);
    }

    // 前端傳入ShippingMethod，建立並回傳對應的 IShippingStrategy
    private static IShippingStrategy createShippingStrategy(String shippingMethod) {
        if ("freeOver".equalsIgnoreCase(shippingMethod)) {
            return new ecommerce.class_.FreeShippingThreshold(3000.0, 80.0);
        }
        return new ecommerce.class_.FlatRateShipping(60.0);
    }

    // 用付款方式與帳號，建立付款處理器，送交後端 IPaymentProcessor.java
    private static IPaymentProcessor createPaymentProcessor(String paymentMethod, String paymentAccount) {
        if ("linepay".equalsIgnoreCase(paymentMethod)) {
            return new LinePayPayment(paymentAccount);
        }
        // 預設為信用卡
        return new CreditCardPayment(paymentAccount);
    }

    // 將 JSON 字串轉換為 Map 或 List 的 Java 物件
    private static Object parseJson(String text) {
        if (text == null) {
            return null;
        }
        return new JsonParser(text).parseValue();
    }

    // 處理字串內的跳脫字元，確保組成 JSON 時的格式安全
    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    // 將未知的物件安全地轉換為整數 (int)
    private static int toInt(Object value) {
        if (value instanceof Number num) {
            return num.intValue();
        }
        // 避免使用 try-catch 處理邏輯以提升效能，改用正則表達式預先檢查
        if (value != null) {
            String strValue = String.valueOf(value).trim();
            // 檢查字串是否為有效整數 (允許負號)
            if (strValue.matches("-?\\d+")) {
                return Integer.parseInt(strValue);
            }
        }
        return 0; // 若格式不符或為 null，給予預設值
    }

    // 將未知的物件安全地轉換為浮點數 (double)
    private static double toDouble(Object value) {
        if (value instanceof Number num) {
            return num.doubleValue();
        }
        // 避免使用 try-catch 處理商業邏輯以提升效能，改用正則表達式預先檢查
        if (value != null) {
            String strValue = String.valueOf(value).trim();
            // 檢查字串是否為有效浮點數 (允許負號與小數點)
            if (strValue.matches("-?\\d+(\\.\\d+)?")) {
                return Double.parseDouble(strValue);
            }
        }
        return 0.0; // 若格式不符或為 null，給予預設值
    }

    // 處理字串內的跳脫字元與逗號，確保寫入 CSV 時的格式正確
    private static String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\r")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }

    // 根據檔案副檔名判斷並回傳對應的 MIME Content-Type
    private static String getContentType(String path) {
        if (path.endsWith(".html")) {
            return "text/html; charset=UTF-8";
        }
        if (path.endsWith(".css")) {
            return "text/css; charset=UTF-8";
        }
        if (path.endsWith(".js")) {
            return "application/javascript; charset=UTF-8";
        }
        if (path.endsWith(".json")) {
            return "application/json; charset=UTF-8";
        }
        if (path.endsWith(".png")) {
            return "image/png";
        }
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        return "text/plain; charset=UTF-8";
    }

    // 自訂的輕量級 JSON 解析器，避免對外部套件的依賴
    private static class JsonParser {
        private final String text;
        private int pos;

        public JsonParser(String text) {
            this.text = text.trim();
            this.pos = 0;
        }

        public Object parseValue() {
            skipWhitespace();
            if (pos >= text.length()) {
                return null;
            }
            char ch = text.charAt(pos);
            if (ch == '{') {
                return parseObject();
            }
            if (ch == '[') {
                return parseArray();
            }
            if (ch == '"') {
                return parseString();
            }
            if (Character.isDigit(ch) || ch == '-') {
                return parseNumber();
            }
            if (text.startsWith("true", pos)) {
                pos += 4;
                return Boolean.TRUE;
            }
            if (text.startsWith("false", pos)) {
                pos += 5;
                return Boolean.FALSE;
            }
            if (text.startsWith("null", pos)) {
                pos += 4;
                return null;
            }
            return null;
        }

        private Map<String, Object> parseObject() {
            Map<String, Object> map = new HashMap<>();
            pos++;
            skipWhitespace();
            while (pos < text.length() && text.charAt(pos) != '}') {
                String key = parseString();
                skipWhitespace();
                if (pos < text.length() && text.charAt(pos) == ':') {
                    pos++;
                }
                skipWhitespace();
                Object value = parseValue();
                map.put(key, value);
                skipWhitespace();
                if (pos < text.length() && text.charAt(pos) == ',') {
                    pos++;
                    skipWhitespace();
                }
            }
            if (pos < text.length() && text.charAt(pos) == '}') {
                pos++;
            }
            return map;
        }

        private List<Object> parseArray() {
            List<Object> list = new ArrayList<>();
            pos++;
            skipWhitespace();
            while (pos < text.length() && text.charAt(pos) != ']') {
                Object value = parseValue();
                list.add(value);
                skipWhitespace();
                if (pos < text.length() && text.charAt(pos) == ',') {
                    pos++;
                    skipWhitespace();
                }
            }
            if (pos < text.length() && text.charAt(pos) == ']') {
                pos++;
            }
            return list;
        }

        private String parseString() {
            pos++;
            StringBuilder sb = new StringBuilder();
            while (pos < text.length()) {
                char ch = text.charAt(pos);
                if (ch == '"') {
                    pos++;
                    break;
                }
                if (ch == '\\') {
                    pos++;
                    if (pos >= text.length()) {
                        break;
                    }
                    char next = text.charAt(pos);
                    switch (next) {
                        // 使用 -> 代替 手動 break;
                        case '"' -> sb.append('"');
                        case '\\' -> sb.append('\\');
                        case '/' -> sb.append('/');
                        case 'b' -> sb.append('\b');
                        case 'f' -> sb.append('\f');
                        case 'n' -> sb.append('\n');
                        case 'r' -> sb.append('\r');
                        case 't' -> sb.append('\t');
                        default -> sb.append(next);
                    }
                    pos++;
                } else {
                    sb.append(ch);
                    pos++;
                }
            }
            return sb.toString();
        }

        private Number parseNumber() {
            int start = pos;
            if (text.charAt(pos) == '-') {
                pos++;
            }
            while (pos < text.length() && Character.isDigit(text.charAt(pos))) {
                pos++;
            }
            boolean isDouble = false;
            if (pos < text.length() && text.charAt(pos) == '.') {
                isDouble = true;
                pos++;
                while (pos < text.length() && Character.isDigit(text.charAt(pos))) {
                    pos++;
                }
            }
            String token = text.substring(start, pos);
            try {
                if (isDouble) {
                    return Double.valueOf(token);
                }
                return Integer.valueOf(token);
            } catch (NumberFormatException e) {
                return 0;
            }
        }

        private void skipWhitespace() {
            while (pos < text.length() && Character.isWhitespace(text.charAt(pos))) {
                pos++;
            }
        }
    }
}
