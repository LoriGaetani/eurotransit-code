package com.example.order.order

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "orders")
open class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long? = null,

    @Column(name = "idempotency_key", nullable = false, unique = true)
    open var idempotencyKey: String = "",

    @Column(name = "product_id", nullable = false)
    open var productId: Long = 0,

    @Column(nullable = false)
    open var quantity: Int = 0,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    open var status: OrderStatus = OrderStatus.CREATED,

    @Column(name = "created_at", nullable = false, updatable = false)
    open var createdAt: Instant = Instant.EPOCH,

    @Column(name = "updated_at", nullable = false)
    open var updatedAt: Instant = Instant.EPOCH,
) {
    @PrePersist
    fun prePersist() {
        val now = Instant.now()
        createdAt = now
        updatedAt = now
    }

    @PreUpdate
    fun preUpdate() {
        updatedAt = Instant.now()
    }
}
