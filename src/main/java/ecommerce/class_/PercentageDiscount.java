package ecommerce.class_;

import ecommerce.interface_.IDiscountStrategy;

// 實作IDiscountStrategy

public class PercentageDiscount implements IDiscountStrategy {
    // 折扣扣抵比例，例如：全館 9 折即為 0.1
    private final double percentage; // 例如：全館 9 折即為 0.1 (折扣扣抵比例)

    // 建構子
    public PercentageDiscount(double percentage) {
        this.percentage = percentage;
    }
    
    // 根據比例計算應扣抵的折扣金額
    @Override
    public double calculateDiscount(double subtotal) {
        // 根據比例計算應扣抵的折扣金額
        return subtotal * percentage;
    }
}
