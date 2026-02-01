package com.payment.order.service;

import com.payment.order.dto.InventoryRequest;
import com.payment.order.dto.OrderRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class InventoryGateway {

    private final RestTemplate restTemplate;

    public InventoryGateway(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String reserve(OrderRequest orderRequest) {
        ResponseEntity<String> inventoryReserveResponse = restTemplate.postForEntity(
                "http://inventory-service/api/v1/inventory/reserve",
                new InventoryRequest(orderRequest.getProductId(), orderRequest.getQuantity()),
                String.class
        );
        return inventoryReserveResponse.getBody();
    }

    public String release(OrderRequest orderRequest) {
        ResponseEntity<String> inventoryReleaseResponse = restTemplate.postForEntity(
                "http://inventory-service/api/v1/inventory/release",
                new InventoryRequest(orderRequest.getProductId(), orderRequest.getQuantity()),
                String.class
        );
        return inventoryReleaseResponse.getBody();
    }

}
