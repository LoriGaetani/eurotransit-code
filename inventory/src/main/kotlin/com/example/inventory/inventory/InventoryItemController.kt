package com.example.inventory.inventory

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/inventory/items")
class InventoryItemController(
    private val inventoryItemService: InventoryItemService,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @GetMapping
    fun findAll(): List<InventoryItemDto> {
        log.info("Received request to list inventory items")
        return inventoryItemService.findAll()
    }

    @GetMapping("/{productId}")
    fun findByProductId(@PathVariable productId: Long): InventoryItemDto {
        log.info("Received request to get inventory item productId={}", productId)
        return inventoryItemService.findByProductId(productId)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody inventoryItemDto: InventoryItemDto): InventoryItemDto {
        log.info("Received request to create inventory item productId={}", inventoryItemDto.productId)
        return inventoryItemService.create(inventoryItemDto)
    }

    @PutMapping("/{productId}")
    fun update(
        @PathVariable productId: Long,
        @RequestBody inventoryItemDto: InventoryItemDto,
    ): InventoryItemDto {
        log.info("Received request to update inventory item productId={}", productId)
        return inventoryItemService.update(productId, inventoryItemDto)
    }

    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable productId: Long) {
        log.info("Received request to delete inventory item productId={}", productId)
        inventoryItemService.delete(productId)
    }
}
