package com.payment.inventory.service;

import com.payment.inventory.entity.Inventory;
import com.payment.inventory.repository.InventoryRepository;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
public class InventoryTxnServiceTest {

    @Autowired
    InventoryRepository inventoryRepository;

    @Autowired
    InventoryTxnService inventoryTxnService;

    @Test
    public void testReserve() throws Exception {
        int threadCount = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        CountDownLatch countDownLatch = new CountDownLatch(threadCount);

        Runnable task = () -> {
            try {
                boolean result = inventoryTxnService.reserveStock("P1", 2);
                System.out.println(Thread.currentThread().getName() + " result = " + result);
            } catch (OptimisticLockException e) {
                System.out.println(Thread.currentThread().getName() + " failed due to optimistic lock");
            } finally {
                countDownLatch.countDown();
            }
        };

        executorService.submit(task);
        executorService.submit(task);

        countDownLatch.await();

        Inventory inventory1 = inventoryRepository.findById("P1").orElseThrow();
        System.out.println("Final Available quantity : " + inventory1.getAvailableQuantity());

    }

    @Test
    public void testReserve_InsufficientQuantity() {
        boolean result = inventoryTxnService.reserveStock("P1", 6);
        System.out.println("Result for insufficient quantity: " + result);
    }

}
