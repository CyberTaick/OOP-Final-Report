package ecommerce;

public class FlatRateShipping implements IShippingStrategy {
    private double rate;

    public FlatRateShipping(double rate) {
        this.rate = rate;
    }

    @Override
    public double calculateShipping(double subtotal) {
        // 固定運費
        return rate;
    }
}
