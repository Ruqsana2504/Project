package com.payment.inventory.controller;

import com.payment.inventory.dto.InventoryRequest;
import com.payment.inventory.entity.IdempotencyRecord;
import com.payment.inventory.repository.IdempotencyRepository;
import com.payment.inventory.service.InventoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
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

        log.info("Received reserve request with idempotencyKey : {} for product : {}", idempotencyKey, requestBody.productId());

        Optional<IdempotencyRecord> record;
        try {
            record = idempotencyRepository.findById(idempotencyKey);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid idempotencyKey");
        }

        if (record.isPresent()) {
            log.debug("Idempotent request. Returning cached response for key: {} with response : {}", idempotencyKey, record.get());
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

    @PostMapping("/release")
    public ResponseEntity<Void> release(@RequestHeader("Idempotency-Key") String idempotencyKey,
                                        @RequestBody InventoryRequest requestBody) throws Exception {

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalAccessException("Missing or empty idempotencyKey header");
        }

        log.info("Received release request with idempotencyKey : {} for product : {}", idempotencyKey, requestBody.productId());

        Optional<IdempotencyRecord> record;
        try {
            record = idempotencyRepository.findById(idempotencyKey);
        } catch (IllegalArgumentException e) {
            throw new IllegalAccessException("Invalid idempotencyKey");
        }

        String response = "";
        if (record.isPresent()) {
            response = record.get().getResponse();
        }

        inventoryService.releaseStock(requestBody);

        idempotencyRepository.save(
                new IdempotencyRecord(idempotencyKey, response)
        );

        return ResponseEntity.ok().build();

    }
}