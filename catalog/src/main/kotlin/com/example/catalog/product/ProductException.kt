package com.example.catalog.product

class ProductException(id: Long) : RuntimeException("Product $id not found")
