package com.payment.inventory.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class InventoryRequest {

    private String productId;

    private int availableQuantity;

}