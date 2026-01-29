package com.payment.inventory.service;

import com.payment.inventory.repository.InventoryRepository;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class InventoryServiceTest {

    @Mock
    InventoryTxnService inventoryTxnService;

    @Mock
    InventoryRepository inventoryRepository;

    @InjectMocks
    InventoryService inventoryService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testInventoryService_SufficientStock() {
        when(inventoryTxnService.reserveStock(anyString(), anyInt())).thenReturn(Boolean.TRUE);
        Boolean result = inventoryService.reserveStockWithRetry("P1", 2);
        Assertions.assertEquals(Boolean.TRUE, result);
    }

    @Test
    public void testInventoryService_InSufficientStock() {
        when(inventoryTxnService.reserveStock(anyString(), anyInt())).thenReturn(Boolean.FALSE);
        Boolean result = inventoryService.reserveStockWithRetry("P1", 6);
        Assertions.assertEquals(Boolean.FALSE, result);
    }

    @Test
    public void testInventoryService_Exception_2ndAttemptSucceed() {
        when(inventoryTxnService.reserveStock(anyString(), anyInt()))
                .thenThrow(new OptimisticLockException("1st attempt fail")) // 1st attempt
                .thenReturn(Boolean.TRUE);                         // 2nd attempt
        Boolean result = inventoryService.reserveStockWithRetry("P1", 4);
        Assertions.assertEquals(Boolean.TRUE, result);
    }

    @Test
    public void testInventoryService_Exception_3rdAttemptSucceed() {
        when(inventoryTxnService.reserveStock(anyString(), anyInt()))
                .thenThrow(new OptimisticLockException("1st attempt fail")) // 1st attempt
                .thenThrow(new OptimisticLockException("2nd attempt fail")) // 2nd attempt
                .thenReturn(Boolean.FALSE);                         // 3rd attempt
        Boolean result = inventoryService.reserveStockWithRetry("P1", 6);
        Assertions.assertEquals(Boolean.FALSE, result);
    }

    @Test
    public void testInventoryService_Exception_AllAttemptsFail() {
        when(inventoryTxnService.reserveStock(anyString(), anyInt()))
                .thenThrow(new OptimisticLockException("1st attempt fail")) // 1st attempt
                .thenThrow(new OptimisticLockException("2nd attempt fail")) // 2nd attempt
                .thenThrow(new OptimisticLockException("3rd attempt fail"));                       // 3rd attempt
        Assertions.assertThrows(OptimisticLockException.class,
                () -> inventoryService.reserveStockWithRetry("P1", 6));
    }
}
