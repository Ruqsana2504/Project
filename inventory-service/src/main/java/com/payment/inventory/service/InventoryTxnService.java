package com.payment.inventory.service;

import com.payment.inventory.entity.Inventory;
import com.payment.inventory.repository.InventoryRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class InventoryTxnService {

    @Autowired
    InventoryRepository inventoryRepository;

    @Transactional
    protected boolean reserveStock(String productId, int quantity) {

        Inventory inventory = inventoryRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (inventory.getAvailableQuantity() < quantity) {
            return false;
        }

        inventory.setAvailableQuantity(inventory.getAvailableQuantity() - quantity);

        System.out.println("Transaction active = " +
                TransactionSynchronizationManager.isActualTransactionActive());

        return true;
    }
}
