package com.payment.inventory.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InventoryRequest {

    private String productId;

    private int availableQuantity;

}