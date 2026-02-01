package com.payment.order.entity;

import com.payment.order.utils.OrderStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@Setter
public class Order {

    @Id
    @GeneratedValue
    private UUID orderId;

    private String userId;

    private String productId;
    private int quantity;
    private BigDecimal amount;

    private String currency;

    @Enumerated(EnumType.STRING)
    //If you omit the @Enumerated(EnumType.STRING) annotation, JPA defaults to using EnumType.ORDINAL.
    //This means the database will store the enum constant's integer index (its position starting at 0) rather than its text name.
    private OrderStatus orderStatus;

    private String idempotencyKey;

    private Instant createdAt;

}