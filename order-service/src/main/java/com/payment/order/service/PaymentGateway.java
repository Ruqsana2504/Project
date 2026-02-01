package com.payment.order.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PaymentGateway {

    public boolean pay(BigDecimal amount) {
        // simulate failure randomly
        if (Math.random() < 0.3) {
            throw new RuntimeException("Payment failed");
        }
        return true;
    }
}
