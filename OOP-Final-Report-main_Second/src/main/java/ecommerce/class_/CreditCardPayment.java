package ecommerce.class_;

import ecommerce.interface_.IPaymentProcessor;

/**
 * 信用卡付款策略
 * 實作 IPaymentProcessor 介面，處理使用信用卡的付款邏輯。
 */
public class CreditCardPayment implements IPaymentProcessor {
    private final String cardNumber;

    public CreditCardPayment(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    @Override
    public boolean processPayment(double amount) {
        System.out.println("💳 正在連線至信用卡公司授權扣款... [卡號: " + cardNumber + "]");
        System.out.println("💰 扣款金額: $" + amount);
        // 模擬等待連線授權
        try { Thread.sleep(1000); } catch (InterruptedException e) {}
        System.out.println("✅ 付款授權成功！");
        return true;
    }
}
