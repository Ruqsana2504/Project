package com.payment.inventory.controller;

import com.payment.inventory.dto.InventoryRequest;
import com.payment.inventory.entity.IdempotencyRecord;
import com.payment.inventory.repository.IdempotencyRepository;
import com.payment.inventory.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryService inventoryService;
    private final IdempotencyRepository idempotencyRepository;

    public InventoryController(InventoryService inventoryService, IdempotencyRepository idempotencyRepository) {
        this.inventoryService = inventoryService;
        this.idempotencyRepository = idempotencyRepository;
    }

    @PostMapping("/reserve")
    public ResponseEntity<String> reserve(@RequestHeader("Idempotency-Key") String idempotencyKey,
                                          @RequestBody InventoryRequest requestBody) {

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return ResponseEntity.badRequest().body("Missing or empty idempotencyKey header");
        }

        Optional<IdempotencyRecord> record;
        try {
            record = idempotencyRepository.findById(idempotencyKey);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid idempotencyKey");
        }

        if (record.isPresent()) {
            return ResponseEntity.ok(record.get().getResponse());
        }

        boolean success = inventoryService.reserveStockWithRetry(requestBody);
        ResponseEntity<String> result = success
                ? ResponseEntity.ok("Reserved")
                : ResponseEntity.badRequest().body("Insufficient stock");

        idempotencyRepository.save(
                new IdempotencyRecord(idempotencyKey, String.valueOf(result.getBody()))
        );
        return result;
    }

}