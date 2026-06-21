package com.example.catalog.product

interface ProductService {
    fun findAll(): List<ProductDto>

    fun findById(id: Long): ProductDto

    fun create(productDto: ProductDto): ProductDto

    fun update(id: Long, productDto: ProductDto): ProductDto

    fun delete(id: Long)
}
