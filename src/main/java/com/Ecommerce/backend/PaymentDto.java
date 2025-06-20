package com.Ecommerce.backend;

public class PaymentDto {
    private String method;
    private String cardNumber;    // may be null for COD
    // getters & setters…


    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }
}
