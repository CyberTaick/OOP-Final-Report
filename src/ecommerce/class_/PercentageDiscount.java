package ecommerce.class_;

import ecommerce.interface_.IDiscountStrategy;

public class PercentageDiscount implements IDiscountStrategy {
    private final double percentage; // 例如：全館 9 折即為 0.1 (折扣扣抵比例)

    public PercentageDiscount(double percentage) {
        this.percentage = percentage;
    }

    @Override
    public double calculateDiscount(double subtotal) {
        // TODO: 根據比例計算應扣抵的折扣金額
        return subtotal * percentage;
    }
}
