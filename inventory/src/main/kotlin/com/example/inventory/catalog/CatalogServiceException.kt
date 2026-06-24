package com.example.inventory.catalog

class CatalogServiceException(
    productId: Long,
    cause: Throwable,
) : RuntimeException("Catalog unavailable while checking product $productId", cause)
