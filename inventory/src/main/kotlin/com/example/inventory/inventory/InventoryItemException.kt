package com.example.inventory.inventory

class InventoryItemException(productId: Long) : RuntimeException("Inventory item $productId not found")
