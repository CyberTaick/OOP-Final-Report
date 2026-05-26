package ecommerce.interface_;

import ecommerce.class_.Product;
import java.util.List;

/**
 * 負責商品資料存取的介面
 */
public interface IProductRepository {
    /**
     * 載入所有商品資料
     */
    List<Product> loadProducts();
}
