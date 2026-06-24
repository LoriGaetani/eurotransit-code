package com.example.inventory.reservation

sealed class ReservationException(message: String) : RuntimeException(message)

class ReservationNotFoundException(idempotencyKey: String) :
    ReservationException("Reservation $idempotencyKey not found")

class ReservationConflictException(idempotencyKey: String) :
    ReservationException("Idempotency key $idempotencyKey was already used for a different reservation")

class InsufficientInventoryException(
    productId: Long,
    requestedQuantity: Int,
    availableQuantity: Int,
) : ReservationException(
    "Insufficient inventory for product $productId: requested $requestedQuantity, available $availableQuantity"
)
