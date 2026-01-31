package com.payment.inventory.controller;

import com.payment.inventory.service.InventoryService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class InventoryControllerTest {

    @Mock
    InventoryService inventoryService;

    @InjectMocks
    InventoryController inventoryController;

    @Test
    public void testReserve() {
        when(inventoryService.reserveStockWithRetry(anyString(), anyInt())).thenReturn(Boolean.FALSE);
        ResponseEntity<String> result = inventoryController.reserve("P1", 6);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }
}
