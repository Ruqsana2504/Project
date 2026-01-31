package com.payment.inventory.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InventoryService {

    @Autowired
    InventoryTxnService txService;

//    private static final int MAX_RETRIES = 3;

    @Retry(name = "inventoryRetry")
    @CircuitBreaker(
            name = "inventoryCB",
            fallbackMethod = "reserveFallback"
    )
    @Transactional
    public boolean reserveStockWithRetry(String productId, int quantity) {
        return txService.reserveStock(productId, quantity);
    }

    public boolean reserveFallback(String productId, int quantity, Throwable ex) {
        System.out.println("Inventory service degraded: " + ex.getMessage());
        return false;
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