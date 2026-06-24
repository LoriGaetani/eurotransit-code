package com.example.inventory.reservation

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size

data class ReservationRequest(
    @field:NotBlank
    @field:Size(max = 255)
    val idempotencyKey: String,

    @field:Positive
    val productId: Long,

    @field:Positive
    val quantity: Int,
)
