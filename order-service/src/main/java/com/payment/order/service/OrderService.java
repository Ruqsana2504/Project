package com.payment.order.service;

import com.payment.order.dto.OrderRequest;
import com.payment.order.entity.Order;
import com.payment.order.repository.OrderRepository;
import com.payment.order.utils.OrderStatus;
import io.micrometer.common.util.StringUtils;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryGateway inventoryGateway;
    private final PaymentGateway paymentGateway;

    public OrderService(OrderRepository orderRepository, InventoryGateway inventoryGateway, PaymentGateway paymentGateway) {
        this.orderRepository = orderRepository;
        this.inventoryGateway = inventoryGateway;
        this.paymentGateway = paymentGateway;
    }

    @Transactional
    public UUID createOrder(OrderRequest orderRequest) {

        // STEP 1 — Create order
        Order order = new Order();
        order.setUserId(orderRequest.getUserId());
        order.setProductId(orderRequest.getProductId());
        order.setOrderStatus(OrderStatus.CREATED);
        order.setQuantity(orderRequest.getQuantity());
        order.setAmount(orderRequest.getAmount());
        order.setCurrency(orderRequest.getCurrency());
        order.setIdempotencyKey(orderRequest.getIdempotencyKey());
        order.setCreatedAt(Instant.now());

        Order order1 = orderRepository.save(order);
        log.debug("Order from db : {}", order1);

        // STEP 2 — Reserve inventory
        String inventoryResponse = inventoryGateway.reserve(orderRequest);
        log.debug("Inventory response: {}", inventoryResponse);
        if (!StringUtils.isBlank(inventoryResponse) && !inventoryResponse.equals("Reserved")) {
            order.setOrderStatus(OrderStatus.FAILED);
            return order.getOrderId();
        }

        try {
            // STEP 3 — Payment
            paymentGateway.pay(orderRequest.getAmount());

            // STEP 4 — Confirm
            order.setOrderStatus(OrderStatus.CONFIRMED);
            return order.getOrderId();

        } catch (Exception e) {
            // STEP 5 — COMPENSATION
            inventoryGateway.release(orderRequest);
            order.setOrderStatus(OrderStatus.FAILED);
            return order.getOrderId();
        }
    }
}