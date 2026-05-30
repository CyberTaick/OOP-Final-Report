package ecommerce.class_;

import ecommerce.interface_.IPaymentProcessor;

/**
 * LINE Pay 付款策略
 * 實作 IPaymentProcessor 介面，處理使用 LINE Pay 的付款邏輯。
 */
public class LinePayPayment implements IPaymentProcessor {
    private final String linePayId;

    public LinePayPayment(String linePayId) {
        this.linePayId = linePayId;
    }

    @Override
    public boolean processPayment(double amount) {
        System.out.println("📱 正在發送請求至 LINE Pay 伺服器... [帳號/ID: " + linePayId + "]");
        System.out.println("💰 扣款金額: $" + amount);
        // 模擬等待連線授權
        try { Thread.sleep(1000); } catch (InterruptedException e) {}
        System.out.println("✅ LINE Pay 付款成功！");
        return true;
    }
}
