package com.example.inventory.inventory

import org.springframework.data.jpa.repository.JpaRepository

interface InventoryItemRepository : JpaRepository<InventoryItem, Long>
