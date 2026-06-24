package com.example.inventory.inventory

data class InventoryItemDto(
    val productId: Long,
    val quantity: Int,
    val version: Long = 0,
)
