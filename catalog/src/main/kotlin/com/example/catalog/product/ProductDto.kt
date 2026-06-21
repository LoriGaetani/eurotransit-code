package com.example.catalog.product

import java.math.BigDecimal

data class ProductDto(
    val id: Long? = null,
    val name: String,
    val description: String,
    val category: String,
    val price: BigDecimal,
)
