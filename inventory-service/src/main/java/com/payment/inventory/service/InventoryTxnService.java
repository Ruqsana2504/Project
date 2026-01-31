package com.payment.inventory.service;

import com.payment.inventory.dto.InventoryRequest;
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
    protected boolean reserveStock(InventoryRequest inventoryRequest) {

        Inventory inventory = inventoryRepository.findById(inventoryRequest.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (inventory.getAvailableQuantity() < inventoryRequest.getAvailableQuantity()) {
            return false;
        }

        inventory.setAvailableQuantity(inventory.getAvailableQuantity() - inventoryRequest.getAvailableQuantity());

        System.out.println("Transaction active = " +
                TransactionSynchronizationManager.isActualTransactionActive());

        return true;
    }
}
