package com.payment.inventory.service;

import com.payment.inventory.entity.Inventory;
import com.payment.inventory.repository.InventoryRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InventoryService {

    @Autowired
    InventoryRepository inventoryRepository;

    private static final int MAX_RETRIES = 3;

    public boolean reserveStockWithRetry(String productId, int quantity) {
        for (int attempts = 1; attempts <= MAX_RETRIES; attempts++) {
            try {
                return reserveStock(productId, quantity);
            } catch (Exception e) {
                if (attempts == MAX_RETRIES) {
                    throw e;
                }
            }
        }
        return false;
    }

    @Transactional
    public boolean reserveStock(String productId, int quantity) {

        Inventory inventory = inventoryRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (inventory.getAvailableQuantity() < quantity) {
            return false;
        }

        inventory.setAvailableQuantity(inventory.getAvailableQuantity() - quantity);

//        inventoryRepository.save(inventory);
        return true;
    }

}
