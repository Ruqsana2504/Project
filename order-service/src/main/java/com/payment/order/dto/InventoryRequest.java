package com.payment.order.dto;

public record InventoryRequest(String productId, int availableQuantity) {
}
