package com.payment.inventory.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InventoryService {

    @Autowired
    InventoryTxnService txService;

    private static final int MAX_RETRIES = 3;

    public boolean reserveStockWithRetry(String productId, int quantity) {
        for (int attempts = 1; attempts <= MAX_RETRIES; attempts++) {
            try {
                return txService.reserveStock(productId, quantity);
            } catch (Exception e) {
                if (attempts == MAX_RETRIES) {
                    throw e;
                }
            }
        }
        return false;
    }

}