package com.payment.inventory.service;

import com.payment.inventory.entity.Inventory;
import com.payment.inventory.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
public class InventoryServiceTest {

    @Autowired
    InventoryRepository inventoryRepository;

    @Autowired
    InventoryService inventoryService;

    @BeforeEach
    public void setup() {
        inventoryRepository.save(new Inventory("P1", 5));
    }

    @Test
    public void testReserve() throws Exception {
        int threadCount = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        CountDownLatch countDownLatch = new CountDownLatch(threadCount);

        Runnable task = () -> {
            try {
                boolean result = inventoryService.reserveStock("P1", 3);
                System.out.println("Reservation result: " + result);
            } finally {
                countDownLatch.countDown();
            }
        };

        executorService.submit(task);
        executorService.submit(task);

        countDownLatch.await();

        Inventory inventory = inventoryRepository.findById("P1").get();
        System.out.println("Final Available quantity : " + inventory.getAvailableQuantity());

    }

}
