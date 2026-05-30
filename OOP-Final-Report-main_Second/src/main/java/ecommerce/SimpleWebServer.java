package ecommerce;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import ecommerce.class_.CartItem;
import ecommerce.class_.CreditCardPayment;
import ecommerce.class_.Order;
import ecommerce.class_.Product;
import ecommerce.class_.ShoppingCart;
import ecommerce.class_.User;
import ecommerce.core.ShoppingSystem;
import ecommerce.data_access_object.CsvProductRepository;
import ecommerce.interface_.IDiscountStrategy;
import ecommerce.interface_.IPaymentProcessor;
import ecommerce.interface_.IShippingStrategy;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleWebServer {
    private static final String CSV_FILE = "products.csv";
    private static final ShoppingSystem system = new ShoppingSystem(new CsvProductRepository(CSV_FILE));

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
    }

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
            body.append(",\"description\":\"").append(escapeJson(p.getDetails())).append("\"");
            body.append(",\"category\":\"").append(escapeJson(inferCategory(p.getName()))).append("\"");
            body.append(",\"images\":[\"https://via.placeholder.com/300?text=").append(escapeJson(p.getName())).append("\"]");
            body.append("}");
            if (i < products.size() - 1) {
                body.append(",");
            }
        }
        body.append("]");
        sendJson(exchange, 200, body.toString());
    }

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
        Order order = new Order("CALC-" + System.currentTimeMillis(), null, cart.getItems(), discountStrategy, shippingStrategy);

        String json = String.format(
            "{\"subtotal\":%.2f,\"discount\":%.2f,\"shipping\":%.2f,\"total\":%.2f}",
            cart.getSubtotal(),
            discountStrategy != null ? discountStrategy.calculateDiscount(cart.getSubtotal()) : 0.0,
            shippingStrategy != null ? shippingStrategy.calculateShipping(cart.getSubtotal()) : 0.0,
            order.calculateTotal()
        );
        sendJson(exchange, 200, json);
    }

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

        Order order = system.createOrder(guest, discountStrategy, shippingStrategy);
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

        String response = String.format("{\"success\":true,\"orderId\":\"%s\",\"total\":%.2f}", order.getOrderId(), order.calculateTotal());
        sendJson(exchange, 200, response);
    }

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
            newProducts.add(new Product(id, name, price, stock));
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
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    private static void addCors(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
    }

    private static void sendNoContent(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(204, -1);
        exchange.close();
    }

    private static void sendMethodNotAllowed(HttpExchange exchange) throws IOException {
        sendJson(exchange, 405, "{\"message\":\"方法不支援\"}");
    }

    private static void sendNotFound(HttpExchange exchange) throws IOException {
        sendJson(exchange, 404, "{\"message\":\"找不到資源\"}");
    }

    private static void sendJson(HttpExchange exchange, int status, String json) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream in = exchange.getRequestBody()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static boolean writeProductsCsv(List<Product> products) {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(CSV_FILE), StandardCharsets.UTF_8)) {
            for (Product p : products) {
                writer.write(String.join(",", escapeCsv(p.getProductId()), escapeCsv(p.getName()), String.valueOf(p.getPrice()), String.valueOf(p.getStockQuantity())));
                writer.newLine();
            }
            writer.flush();
            return true;
        } catch (IOException e) {
            System.err.println("CSV 寫入失敗: " + e.getMessage());
            return false;
        }
    }

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

    private static IShippingStrategy createShippingStrategy(String shippingMethod) {
        if ("freeOver".equalsIgnoreCase(shippingMethod)) {
            return new ecommerce.class_.FreeShippingThreshold(3000.0, 80.0);
        }
        return new ecommerce.class_.FlatRateShipping(60.0);
    }

    private static IPaymentProcessor createPaymentProcessor(String paymentMethod, String paymentAccount) {
        return new ecommerce.class_.CreditCardPayment(paymentAccount);
    }

    private static String inferCategory(String name) {
        if (name == null) {
            return "一般";
        }
        String lower = name.toLowerCase();
        if (lower.contains("滑鼠") || lower.contains("鼠")) {
            return "周邊";
        }
        if (lower.contains("鍵盤") || lower.contains("鍵")) {
            return "鍵盤";
        }
        if (lower.contains("充電") || lower.contains("線")) {
            return "充電";
        }
        return "一般";
    }

    private static Object parseJson(String text) {
        if (text == null) {
            return null;
        }
        return new JsonParser(text).parseValue();
    }

    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    private static int toInt(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception e) {
            return 0;
        }
    }

    private static double toDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (Exception e) {
            return 0.0;
        }
    }

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
                        case '"': sb.append('"'); break;
                        case '\\': sb.append('\\'); break;
                        case '/': sb.append('/'); break;
                        case 'b': sb.append('\b'); break;
                        case 'f': sb.append('\f'); break;
                        case 'n': sb.append('\n'); break;
                        case 'r': sb.append('\r'); break;
                        case 't': sb.append('\t'); break;
                        default: sb.append(next); break;
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
                    return Double.parseDouble(token);
                }
                return Integer.parseInt(token);
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
