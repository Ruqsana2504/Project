package com.payment.order.exception;

public class PaymentFailedException extends RuntimeException {

    public PaymentFailedException(String msg) {
        super(msg);
    }
}
