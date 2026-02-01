package com.payment.order.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class OrderRequest {

    private String userId;

    private String productId;

    private int quantity;

    private BigDecimal amount;

    private String currency;

    private String idempotencyKey;

}