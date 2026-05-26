package ecommerce.class_;

import ecommerce.interface_.IShippingStrategy;

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
