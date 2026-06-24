package com.example.inventory.inventory

interface InventoryItemService {
    fun findAll(): List<InventoryItemDto>

    fun findByProductId(productId: Long): InventoryItemDto

    fun create(inventoryItemDto: InventoryItemDto): InventoryItemDto

    fun update(productId: Long, inventoryItemDto: InventoryItemDto): InventoryItemDto

    fun delete(productId: Long)
}
