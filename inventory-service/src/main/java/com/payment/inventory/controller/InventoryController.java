package com.payment.inventory.controller;

import com.payment.inventory.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/reserve")
    public ResponseEntity<String> reserve(@RequestParam String productId, @RequestParam int quantity) {
        boolean success = inventoryService.reserveStockWithRetry(productId, quantity);
        return success ? ResponseEntity.ok("Reserved") : ResponseEntity.badRequest().body("Insufficient stock");
    }

}
