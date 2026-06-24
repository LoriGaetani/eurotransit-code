package com.example.inventory.reservation

import com.example.inventory.catalog.CatalogClient
import com.example.inventory.inventory.InventoryItem
import com.example.inventory.inventory.InventoryItemException
import com.example.inventory.inventory.InventoryItemRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ReservationServiceImpl(
    private val reservationRepository: ReservationRepository,
    private val inventoryItemRepository: InventoryItemRepository,
    private val catalogClient: CatalogClient,
) : ReservationService {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional(readOnly = true)
    override fun findAll(): List<ReservationDto> {
        val reservations = reservationRepository.findAll()
        log.info("Found {} reservations", reservations.size)
        return reservations.map { it.toDto() }
    }

    @Transactional(readOnly = true)
    override fun findByIdempotencyKey(idempotencyKey: String): ReservationDto {
        log.debug("Looking up reservation idempotencyKey={}", idempotencyKey)
        return reservationRepository.findRequiredByIdempotencyKey(idempotencyKey).toDto()
    }

    @Transactional
    override fun reserve(request: ReservationRequest): ReservationResult {
        val existingReservation = reservationRepository.findById(request.idempotencyKey).orElse(null)
        if (existingReservation != null) {
            log.info("Returning idempotent reservation idempotencyKey={}", request.idempotencyKey)
            if (!existingReservation.matches(request)) {
                log.warn("Idempotency conflict idempotencyKey={}", request.idempotencyKey)
                throw ReservationConflictException(request.idempotencyKey)
            }

            return ReservationResult(reservation = existingReservation.toDto(), created = false)
        }

        catalogClient.assertProductExists(request.productId)

        val inventoryItem = inventoryItemRepository.findRequiredByProductId(request.productId)
        if (inventoryItem.quantity < request.quantity) {
            log.warn(
                "Insufficient inventory productId={} requested={} available={}",
                request.productId,
                request.quantity,
                inventoryItem.quantity,
            )
            throw InsufficientInventoryException(
                productId = request.productId,
                requestedQuantity = request.quantity,
                availableQuantity = inventoryItem.quantity,
            )
        }

        inventoryItem.quantity -= request.quantity
        inventoryItemRepository.saveAndFlush(inventoryItem)

        val reservation = reservationRepository.saveAndFlush(
            Reservation(
                idempotencyKey = request.idempotencyKey,
                productId = request.productId,
                quantityReserved = request.quantity,
                status = ReservationStatus.CONFIRMED,
            )
        )

        log.info(
            "Reserved inventory idempotencyKey={} productId={} quantity={}",
            reservation.idempotencyKey,
            reservation.productId,
            reservation.quantityReserved,
        )
        return ReservationResult(reservation = reservation.toDto(), created = true)
    }

    private fun ReservationRepository.findRequiredByIdempotencyKey(idempotencyKey: String): Reservation =
        findById(idempotencyKey).orElseThrow {
            log.warn("Reservation idempotencyKey={} not found", idempotencyKey)
            ReservationNotFoundException(idempotencyKey)
        }

    private fun InventoryItemRepository.findRequiredByProductId(productId: Long): InventoryItem =
        findById(productId).orElseThrow {
            log.warn("Inventory item productId={} not found", productId)
            InventoryItemException(productId)
        }

    private fun Reservation.matches(request: ReservationRequest): Boolean =
        productId == request.productId && quantityReserved == request.quantity

    private fun Reservation.toDto(): ReservationDto =
        ReservationDto(
            idempotencyKey = idempotencyKey,
            productId = productId,
            quantityReserved = quantityReserved,
            status = status,
        )
}
