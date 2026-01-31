package com.payment.order.service;

import com.payment.order.dto.OrderRequest;
import com.payment.order.entity.Order;
import com.payment.order.repository.OrderRepository;
import com.payment.order.utils.OrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class OrderService {

    @Autowired
    OrderRepository orderRepository;

    public Order createOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setUserId(orderRequest.getUserId());
        order.setProductId(orderRequest.getProductId());
        order.setOrderStatus(OrderStatus.CREATED);
        order.setQuantity(orderRequest.getQuantity());
        order.setAmount(orderRequest.getAmount());
        order.setCreatedAt(Instant.now());
        return orderRepository.save(order);
    }

    public Order getOrderById(UUID id) {
        return orderRepository.getById(id);
    }

}
