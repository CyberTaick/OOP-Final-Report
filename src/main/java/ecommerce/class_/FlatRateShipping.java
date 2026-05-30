package ecommerce.class_;

import ecommerce.interface_.IShippingStrategy;

// 固定運費策略類別，實作 IShippingStrategy 介面

public class FlatRateShipping implements IShippingStrategy {
    private final double rate;

    public FlatRateShipping(double rate) {
        this.rate = rate;
    }

    @Override
    public double calculateShipping(double subtotal) {
        // 固定運費
        return rate;
    }
}
