package com.payment.order.controller;

import com.payment.order.dto.OrderRequest;
import com.payment.order.entity.Order;
import com.payment.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    OrderService orderService;

    @GetMapping("/{id}")
    public Order getOrderById(UUID id) {
        return new Order();
    }

    @PostMapping
    public Order createOrder(OrderRequest orderRequest) {
        return orderService.createOrder(orderRequest);
    }
}
