package com.payment.inventory.service;

import com.payment.inventory.entity.Inventory;
import com.payment.inventory.repository.InventoryRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
public class InventoryServiceTest {

    @Autowired
    InventoryRepository inventoryRepository;

    @Autowired
    InventoryService inventoryService;

    @Autowired
    EntityManager entityManager;

    @BeforeEach
    public void setup() {
        inventoryRepository.deleteAll();
        inventoryRepository.flush();
        entityManager.clear();

        // data.sql is also executing so it is showing PK error.
        // Either comment this line or add spring.sql.init.mode=never in application-test.yml
//        inventoryRepository.saveAndFlush(new Inventory("P1", 5));

    }

    @Test
    public void testReserve() throws Exception {
        int threadCount = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        CountDownLatch countDownLatch = new CountDownLatch(threadCount);

        Runnable task = () -> {
            try {
                boolean result = inventoryService.reserveStock("P1", 2);
                System.out.println(Thread.currentThread().getName() + " result = " + result);
            } catch (ObjectOptimisticLockingFailureException e) {
                System.out.println(Thread.currentThread().getName() + " failed due to optimistic lock");
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
