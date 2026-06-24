package com.example.inventory.reservation

interface ReservationService {
    fun findAll(): List<ReservationDto>

    fun findByIdempotencyKey(idempotencyKey: String): ReservationDto

    fun reserve(request: ReservationRequest): ReservationResult
}
