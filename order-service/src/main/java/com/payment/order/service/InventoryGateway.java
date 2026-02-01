package com.payment.order.service;

import com.payment.order.dto.InventoryRequest;
import com.payment.order.dto.OrderRequest;
import lombok.extern.slf4j.Slf4j;
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
        try {
            inventoryReserveResponse = restTemplate.postForEntity(
                    "http://localhost:8081/inventory/reserve",
                    new InventoryRequest(orderRequest.getProductId(), orderRequest.getQuantity()),
                    String.class
            );
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null != inventoryReserveResponse ? inventoryReserveResponse.getBody() : null;
    }

    public String release(OrderRequest orderRequest) {
        ResponseEntity<String> inventoryReleaseResponse = null;
        try {
            inventoryReleaseResponse = restTemplate.postForEntity(
                    "http://localhost:8081/inventory/release",
                    new InventoryRequest(orderRequest.getProductId(), orderRequest.getQuantity()),
                    String.class
            );
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null != inventoryReleaseResponse ? inventoryReleaseResponse.getBody() : null;
    }

}
