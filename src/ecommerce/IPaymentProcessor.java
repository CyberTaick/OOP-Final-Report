package ecommerce;

public interface IPaymentProcessor {
    boolean processPayment(double amount);
}
