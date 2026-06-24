package com.example.inventory.catalog

class CatalogProductException(productId: Long) : RuntimeException("Product $productId not found in catalog")
