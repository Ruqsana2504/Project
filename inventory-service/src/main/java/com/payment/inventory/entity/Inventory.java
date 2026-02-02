package com.payment.inventory.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@Table(name = "inventory")
public class Inventory {

    @Id
    private String productId;

    private int availableQuantity;

    @Version
    private Long version;

}
