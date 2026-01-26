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

    @Transactional
    public boolean reserveStock(String productId, int quantity) {

        Inventory inventory = inventoryRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (inventory.getAvailableQuantity() < quantity) {
            return false;
        }

        inventory.setAvailableQuantity(inventory.getAvailableQuantity() - quantity);

        inventoryRepository.save(inventory);
        return true;
    }

}
