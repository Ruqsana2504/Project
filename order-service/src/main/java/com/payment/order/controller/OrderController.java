package com.payment.order.controller;

import com.payment.order.dto.OrderRequest;
import com.payment.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    OrderService orderService;

    @PostMapping
    public ResponseEntity<String> createOrder(OrderRequest orderRequest) {
        UUID orderId = orderService.createOrder(orderRequest);
        return ResponseEntity.ok("Order placed. ID = " + orderId);
    }
}
