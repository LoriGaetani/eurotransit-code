package com.example.order.order

enum class OrderStatus {
    CREATED,
    INVENTORY_RESERVED,
    PAYMENT_AUTHORIZED,
    CONFIRMED,
    FAILED,
    CANCELLED,
}
