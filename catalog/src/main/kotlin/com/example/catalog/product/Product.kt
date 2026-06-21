package com.example.catalog.product

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity
@Table(name = "products")
open class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long? = null,

    @Column(nullable = false)
    open var name: String = "",

    @Column(nullable = false, columnDefinition = "text")
    open var description: String = "",

    @Column(nullable = false)
    open var category: String = "",

    @Column(nullable = false, precision = 19, scale = 2)
    open var price: BigDecimal = BigDecimal.ZERO,
)
