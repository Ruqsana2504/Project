package com.payment.order.dto;

import com.payment.order.utils.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
public class OrderRequest {

    private String userId;

    private String productId;

    private int quantity;

    private BigDecimal amount;

    private String currency;

    private OrderStatus orderStatus;

    private String idempotencyKey;

    private Instant createdAt;

}