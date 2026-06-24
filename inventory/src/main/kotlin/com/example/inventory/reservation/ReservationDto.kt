package com.example.inventory.reservation

data class ReservationDto(
    val idempotencyKey: String,
    val productId: Long,
    val quantityReserved: Int,
    val status: ReservationStatus,
)
