package com.payment.inventory.service;

import com.payment.inventory.dto.InventoryRequest;
import com.payment.inventory.entity.Inventory;
import com.payment.inventory.repository.InventoryRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class InventoryService {

    private final InventoryTxnService txService;
    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryTxnService txService, InventoryRepository inventoryRepository) {
        this.txService = txService;
        this.inventoryRepository = inventoryRepository;
    }

//    private static final int MAX_RETRIES = 3;

    @Retry(name = "inventoryRetry")
    @CircuitBreaker(
            name = "inventoryCB",
            fallbackMethod = "reserveFallback"
    )
    @Transactional
    public boolean reserveStockWithRetry(InventoryRequest inventoryRequest) {
        log.info("Trying reserve for {}", inventoryRequest.productId());
        return txService.reserveStock(inventoryRequest);
    }

    public boolean reserveFallback(InventoryRequest inventoryRequest, Throwable ex) {
        log.info("Inventory service degraded : {} {}", inventoryRequest.productId(), ex.getMessage());
        return false;
    }

    @Transactional
    public void releaseStock(InventoryRequest inventoryRequest) {
        Inventory inventory = inventoryRepository.findById(inventoryRequest.productId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        inventory.setAvailableQuantity(
                inventory.getAvailableQuantity() + inventoryRequest.availableQuantity()
        );
    }


//    public boolean reserveStockWithRetry(String productId, int quantity) {
//        for (int attempts = 1; attempts <= MAX_RETRIES; attempts++) {
//            try {
//                return txService.reserveStock(productId, quantity);
//            } catch (Exception e) {
//                if (attempts == MAX_RETRIES) {
//                    throw e;
//                }
//            }
//        }
//        return false;
//    }

}