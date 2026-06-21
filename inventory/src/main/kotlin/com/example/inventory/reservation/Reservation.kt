package com.example.inventory.reservation

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "reservations")
open class Reservation(
    @Id
    @Column(name = "idempotency_key", nullable = false)
    open var idempotencyKey: String = "",

    @Column(name = "product_id", nullable = false)
    open var productId: Long = 0,

    @Column(name = "quantity_reserved", nullable = false)
    open var quantityReserved: Int = 0,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    open var status: ReservationStatus = ReservationStatus.PENDING,
)
