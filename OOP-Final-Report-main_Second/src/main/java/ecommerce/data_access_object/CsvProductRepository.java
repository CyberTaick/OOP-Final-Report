package ecommerce.data_access_object;

import ecommerce.class_.Product;
import ecommerce.interface_.IProductRepository;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 負責從 CSV 檔案讀取商品資料的實作類別
 */
public class CsvProductRepository implements IProductRepository {
    private final String csvFilePath;

    public CsvProductRepository(String csvFilePath) {
        this.csvFilePath = csvFilePath;
    }

    @Override
    public List<Product> loadProducts() {
        List<Product> products = new ArrayList<>();
        String line;
        String cvsSplitBy = ",";

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(csvFilePath), StandardCharsets.UTF_8))) {
            // 如果您的 CSV 有標題列，可以在這裡加上 br.readLine() 跳過第一行
            
            while ((line = br.readLine()) != null) {
                // 略過空行
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                String[] data = line.split(cvsSplitBy);
                
                // 確保格式正確 (假設有四個欄位：ID, Name, Price, Stock)
                if (data.length >= 4) {
                    String id = data[0].trim();
                    String name = data[1].trim();
                    double price = Double.parseDouble(data[2].trim());
                    int stock = Integer.parseInt(data[3].trim());
                    
                    products.add(new Product(id, name, price, stock));
                }
            }
        } catch (IOException e) {
            System.err.println("讀取 CSV 失敗：" + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("CSV 數字格式錯誤：" + e.getMessage());
        }
        
        return products;
    }
}
