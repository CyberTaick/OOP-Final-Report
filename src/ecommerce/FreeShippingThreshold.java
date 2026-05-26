package ecommerce;

public class FreeShippingThreshold implements IShippingStrategy {
    private double threshold;
    private double standardRate;

    public FreeShippingThreshold(double threshold, double standardRate) {
        this.threshold = threshold; // 滿額免運的門檻
        this.standardRate = standardRate; // 未達門檻時的運費
    }

    @Override
    public double calculateShipping(double subtotal) {
        // TODO: 滿額免運邏輯
        if (subtotal >= threshold) {
            return 0.0;
        }
        return standardRate;
    }
}
