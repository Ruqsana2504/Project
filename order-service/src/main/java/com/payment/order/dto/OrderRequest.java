package com.payment.order.dto;

import com.payment.order.utils.OrderStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
public class OrderRequest {

    private String orderId;

    private String userId;

    private String productId;

    private int quantity;

    private BigDecimal amount;

    private String currency;

    private OrderStatus orderStatus;

    private String idempotencyKey;

}