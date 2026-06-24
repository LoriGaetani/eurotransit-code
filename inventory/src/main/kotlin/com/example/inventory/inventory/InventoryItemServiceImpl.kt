package com.example.inventory.inventory

import com.example.inventory.catalog.CatalogClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class InventoryItemServiceImpl(
    private val inventoryItemRepository: InventoryItemRepository,
    private val catalogClient: CatalogClient,
) : InventoryItemService {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional(readOnly = true)
    override fun findAll(): List<InventoryItemDto> {
        val inventoryItems = inventoryItemRepository.findAll()
        log.info("Found {} inventory items", inventoryItems.size)
        return inventoryItems.map { it.toDto() }
    }

    @Transactional(readOnly = true)
    override fun findByProductId(productId: Long): InventoryItemDto {
        log.debug("Looking up inventory item productId={}", productId)
        return inventoryItemRepository.findRequiredByProductId(productId).toDto()
    }

    @Transactional
    override fun create(inventoryItemDto: InventoryItemDto): InventoryItemDto {
        catalogClient.assertProductExists(inventoryItemDto.productId)

        val savedInventoryItem = inventoryItemRepository.save(inventoryItemDto.toEntity())
        log.info("Created inventory item productId={} quantity={}", savedInventoryItem.productId, savedInventoryItem.quantity)
        return savedInventoryItem.toDto()
    }

    @Transactional
    override fun update(productId: Long, inventoryItemDto: InventoryItemDto): InventoryItemDto {
        log.debug("Updating inventory item productId={}", productId)
        catalogClient.assertProductExists(productId)

        val inventoryItem = inventoryItemRepository.findRequiredByProductId(productId)

        inventoryItem.quantity = inventoryItemDto.quantity

        val savedInventoryItem = inventoryItemRepository.save(inventoryItem)
        log.info("Updated inventory item productId={} quantity={}", savedInventoryItem.productId, savedInventoryItem.quantity)
        return savedInventoryItem.toDto()
    }

    @Transactional
    override fun delete(productId: Long) {
        log.debug("Deleting inventory item productId={}", productId)
        val inventoryItem = inventoryItemRepository.findRequiredByProductId(productId)
        inventoryItemRepository.delete(inventoryItem)
        log.info("Deleted inventory item productId={}", productId)
    }

    private fun InventoryItemRepository.findRequiredByProductId(productId: Long): InventoryItem =
        findById(productId).orElseThrow {
            log.warn("Inventory item productId={} not found", productId)
            InventoryItemException(productId)
        }

    private fun InventoryItem.toDto(): InventoryItemDto =
        InventoryItemDto(
            productId = productId,
            quantity = quantity,
            version = version,
        )

    private fun InventoryItemDto.toEntity(): InventoryItem =
        InventoryItem(
            productId = productId,
            quantity = quantity,
            version = version,
        )
}
