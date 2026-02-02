//package com.payment.inventory.service;
//
//import com.payment.inventory.dto.InventoryRequest;
//import com.payment.inventory.entity.Inventory;
//import com.payment.inventory.repository.InventoryRepository;
//import jakarta.persistence.OptimisticLockException;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ActiveProfiles;
//
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//@Slf4j
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
//@ActiveProfiles("test")
//public class InventoryTxnServiceTest {
//
//    @Autowired
//    InventoryRepository inventoryRepository;
//
//    @Autowired
//    InventoryTxnService inventoryTxnService;
//
//    @Test
//    public void testReserve() throws Exception {
//        int threadCount = 2;
//        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
//
//        CountDownLatch countDownLatch = new CountDownLatch(threadCount);
//
//        Runnable task = () -> {
//            try {
//                boolean result = inventoryTxnService.reserveStock(new InventoryRequest("P1", 3));
//                log.info("{} result = {} ", Thread.currentThread().getName(), result);
//            } catch (OptimisticLockException e) {
//                log.info(" {} failed due to optimistic lock.", Thread.currentThread().getName());
//            } finally {
//                countDownLatch.countDown();
//            }
//        };
//
//        executorService.submit(task);
//        executorService.submit(task);
//
//        countDownLatch.await();
//
//        Inventory inventory1 = inventoryRepository.findById("P1").orElseThrow();
//        log.info("Final Available quantity : {}", inventory1.getAvailableQuantity());
//
//    }
//
//    @Test
//    public void testReserve_InsufficientQuantity() {
//        Assertions.assertThrows(InsufficientStockException.class, () -> inventoryTxnService.reserveStock(new InventoryRequest("P1", 6)));
//    }
//
//}