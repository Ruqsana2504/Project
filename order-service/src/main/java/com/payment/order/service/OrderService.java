package com.payment.order.service;

import com.payment.order.dto.OrderRequest;
import com.payment.order.entity.Order;
import com.payment.order.mapper.OrderMapper;
import com.payment.order.repository.OrderRepository;
import com.payment.order.utils.OrderStatus;
import io.micrometer.common.util.StringUtils;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
public class OrderService {

    private final OrderMapper orderMapper;
    private final OrderRepository orderRepository;
    private final InventoryGateway inventoryGateway;
    private final PaymentGateway paymentGateway;

    public OrderService(OrderMapper orderMapper, OrderRepository orderRepository, InventoryGateway inventoryGateway, PaymentGateway paymentGateway) {
        this.orderMapper = orderMapper;
        this.orderRepository = orderRepository;
        this.inventoryGateway = inventoryGateway;
        this.paymentGateway = paymentGateway;
    }

    @Transactional
    public OrderRequest createOrder(OrderRequest orderRequest) {

        // STEP 1 — Create order
        Order order = new Order();
        order.setOrderId(UUID.randomUUID().toString());
        order.setUserId(orderRequest.getUserId());
        order.setProductId(orderRequest.getProductId());
        order.setOrderStatus(OrderStatus.CREATED);
        order.setQuantity(orderRequest.getQuantity());
        order.setAmount(orderRequest.getAmount());
        order.setCurrency(orderRequest.getCurrency());
        order.setIdempotencyKey(orderRequest.getIdempotencyKey());
        order.setCreatedAt(Instant.now());

        Order orderDetailsFromDB = orderRepository.save(order);
        log.debug("Order from db : {}", orderDetailsFromDB);

        // STEP 2 — Reserve inventory
        String inventoryResponse = inventoryGateway.reserve(orderRequest);
        log.debug("Inventory response: {}", inventoryResponse);
        if (!StringUtils.isBlank(inventoryResponse) && !inventoryResponse.equals("Reserved")) {
            orderDetailsFromDB.setOrderStatus(OrderStatus.FAILED);
        }

        try {
            // STEP 3 — Payment
            paymentGateway.pay();

            // STEP 4 — Confirm
            orderDetailsFromDB.setOrderStatus(OrderStatus.CONFIRMED);
            log.info("Payment successful and Order status updated to CONFIRMED for orderId {}", order.getOrderId());

        } catch (Exception e) {
            // STEP 5 — COMPENSATION
            log.error("Payment failed, releasing inventory for orderId {}: {}", order.getOrderId(), e.getMessage());
            inventoryGateway.release(orderRequest);
            orderDetailsFromDB.setOrderStatus(OrderStatus.FAILED);
            log.info("Order status updated to FAILED for orderId {}", order.getOrderId());
        }
        log.info("Is Transaction active = {} ", TransactionSynchronizationManager.isActualTransactionActive());

        return orderMapper.toDto(orderDetailsFromDB);
    }
}