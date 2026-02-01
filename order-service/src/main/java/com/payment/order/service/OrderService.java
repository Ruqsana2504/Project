package com.payment.order.service;

import com.payment.order.dto.OrderRequest;
import com.payment.order.entity.Order;
import com.payment.order.repository.OrderRepository;
import com.payment.order.utils.OrderStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

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

    public UUID createOrder(OrderRequest orderRequest) {

        // STEP 1 — Create order
        Order order = new Order();
        order.setUserId(orderRequest.getUserId());
        order.setProductId(orderRequest.getProductId());
        order.setOrderStatus(OrderStatus.CREATED);
        order.setQuantity(orderRequest.getQuantity());
        order.setAmount(orderRequest.getAmount());
        order.setCreatedAt(Instant.now());

        orderRepository.save(order);

        // STEP 2 — Reserve inventory
        String inventoryResponse = inventoryGateway.reserve(orderRequest);
        if (!inventoryResponse.equals("Reserved")) {
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