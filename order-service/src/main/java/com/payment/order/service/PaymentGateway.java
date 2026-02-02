package com.payment.order.service;

import com.payment.order.exception.PaymentFailedException;
import org.springframework.stereotype.Component;

@Component
public class PaymentGateway {

    public void pay() {
        // simulate failure randomly
        if (Math.random() < 0.3) {
            throw new PaymentFailedException("Payment failed");
        }
    }
}
