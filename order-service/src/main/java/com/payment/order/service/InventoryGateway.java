package com.payment.order.service;

import com.payment.order.dto.InventoryRequest;
import com.payment.order.dto.OrderRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class InventoryGateway {

    private final RestTemplate restTemplate;

    public InventoryGateway(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String reserve(OrderRequest orderRequest) {
        ResponseEntity<String> inventoryReserveResponse = null;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Idempotency-Key", orderRequest.getIdempotencyKey()); // In real scenario, generate unique key per request

        InventoryRequest inventoryRequest = new InventoryRequest(orderRequest.getProductId(), orderRequest.getQuantity());

        try {
            inventoryReserveResponse = restTemplate.exchange(
                    "http://localhost:8081/inventory/reserve",
                    HttpMethod.POST,
                    new HttpEntity<>(inventoryRequest, headers),
                    String.class
            );
        } catch (Exception e) {
            log.error("Error from Inventory Reserve : {}", e.getMessage());
        }
        return null != inventoryReserveResponse ? inventoryReserveResponse.getBody() : null;
    }

    public Void release(OrderRequest orderRequest) {
        ResponseEntity<Void> inventoryReleaseResponse = null;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Idempotency-Key", orderRequest.getIdempotencyKey()); // In real scenario, generate unique key per request

        InventoryRequest inventoryRequest = new InventoryRequest(orderRequest.getProductId(), orderRequest.getQuantity());

        try {
            inventoryReleaseResponse = restTemplate.exchange(
                    "http://localhost:8081/inventory/release",
                    HttpMethod.POST,
                    new HttpEntity<>(inventoryRequest, headers),
                    Void.class
            );
        } catch (Exception e) {
            log.error("Error from Inventory Release : {}", e.getMessage());
        }
        return null != inventoryReleaseResponse ? inventoryReleaseResponse.getBody() : null;
    }

}
