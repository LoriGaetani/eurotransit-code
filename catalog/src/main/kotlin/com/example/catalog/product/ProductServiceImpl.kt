package com.example.catalog.product

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductServiceImpl(
    private val productRepository: ProductRepository,
) : ProductService {

    @Transactional(readOnly = true)
    override fun findAll(): List<ProductDto> =
        productRepository.findAll().map { it.toDto() }

    @Transactional(readOnly = true)
    override fun findById(id: Long): ProductDto =
        productRepository.findRequiredById(id).toDto()

    @Transactional
    override fun create(productDto: ProductDto): ProductDto =
        productRepository.save(productDto.toEntity(id = null)).toDto()

    @Transactional
    override fun update(id: Long, productDto: ProductDto): ProductDto {
        val product = productRepository.findRequiredById(id)

        product.name = productDto.name
        product.description = productDto.description
        product.category = productDto.category
        product.price = productDto.price

        return productRepository.save(product).toDto()
    }

    @Transactional
    override fun delete(id: Long) {
        val product = productRepository.findRequiredById(id)
        productRepository.delete(product)
    }

    private fun ProductRepository.findRequiredById(id: Long): Product =
        findById(id).orElseThrow { ProductException(id) }

    private fun Product.toDto(): ProductDto =
        ProductDto(
            id = id,
            name = name,
            description = description,
            category = category,
            price = price,
        )

    private fun ProductDto.toEntity(id: Long?): Product =
        Product(
            id = id,
            name = name,
            description = description,
            category = category,
            price = price,
        )
}

