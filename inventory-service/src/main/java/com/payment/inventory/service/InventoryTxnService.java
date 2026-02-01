package com.payment.inventory.service;

import com.payment.inventory.dto.InventoryRequest;
import com.payment.inventory.entity.Inventory;
import com.payment.inventory.repository.InventoryRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
public class InventoryTxnService {

    @Autowired
    InventoryRepository inventoryRepository;

    @Transactional
    protected boolean reserveStock(InventoryRequest inventoryRequest) {

        Inventory inventory = inventoryRepository.findById(inventoryRequest.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        log.debug("Inventory details retrieved: {}", inventory);

        if (inventory.getAvailableQuantity() < inventoryRequest.getAvailableQuantity()) {
            throw new InsufficientStockException("Insufficient stock for product: " + inventoryRequest.getProductId());
        }

        inventory.setAvailableQuantity(inventory.getAvailableQuantity() - inventoryRequest.getAvailableQuantity());

        log.info("Transaction active = {} ", TransactionSynchronizationManager.isActualTransactionActive());

        return true;
    }
}
