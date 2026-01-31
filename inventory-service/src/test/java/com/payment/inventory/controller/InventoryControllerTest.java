package com.payment.inventory.controller;

import com.payment.inventory.dto.InventoryRequest;
import com.payment.inventory.repository.IdempotencyRepository;
import com.payment.inventory.service.InventoryService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class InventoryControllerTest {

    @Mock
    InventoryService inventoryService;

    @Mock
    IdempotencyRepository idempotencyRepository;

    @InjectMocks
    InventoryController inventoryController;

    @Test
    public void testReserve() {
        when(inventoryService.reserveStockWithRetry(any())).thenReturn(Boolean.FALSE);
        ResponseEntity<String> result = inventoryController.reserve("123", new InventoryRequest("P1", 6));
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }
}
