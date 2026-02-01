package com.payment.inventory.service;

import com.payment.inventory.dto.InventoryRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
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
    public boolean reserveStockWithRetry(InventoryRequest inventoryRequest) {
        log.info("Trying reserve for {}", inventoryRequest.getProductId());
        return txService.reserveStock(inventoryRequest);
    }

    public boolean reserveFallback(InventoryRequest inventoryRequest, Throwable ex) {
        log.info("Inventory service degraded : {} {}", inventoryRequest.getProductId(), ex.getMessage());
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