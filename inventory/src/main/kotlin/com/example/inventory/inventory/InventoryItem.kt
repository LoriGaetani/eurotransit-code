package com.example.inventory.inventory

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.Version

@Entity
@Table(name = "inventory")
open class InventoryItem(
    @Id
    @Column(name = "product_id", nullable = false)
    open var productId: Long = 0,

    @Column(nullable = false)
    open var quantity: Int = 0,

    @Version
    open var version: Long = 0,
)
