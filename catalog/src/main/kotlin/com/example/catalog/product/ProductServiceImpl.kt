package com.example.catalog.product

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductServiceImpl(
    private val productRepository: ProductRepository,
) : ProductService {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional(readOnly = true)
    override fun findAll(): List<ProductDto> {
        val products = productRepository.findAll()
        log.info("Found {} products", products.size)
        return products.map { it.toDto() }
    }

    @Transactional(readOnly = true)
    override fun findById(id: Long): ProductDto {
        log.debug("Looking up product id={}", id)
        return productRepository.findRequiredById(id).toDto()
    }

    @Transactional
    override fun create(productDto: ProductDto): ProductDto {
        val savedProduct = productRepository.save(productDto.toEntity(id = null))
        log.info("Created product id={} name={}", savedProduct.id, savedProduct.name)
        return savedProduct.toDto()
    }

    @Transactional
    override fun update(id: Long, productDto: ProductDto): ProductDto {
        log.debug("Updating product id={}", id)
        val product = productRepository.findRequiredById(id)

        product.name = productDto.name
        product.description = productDto.description
        product.category = productDto.category
        product.price = productDto.price

        val savedProduct = productRepository.save(product)
        log.info("Updated product id={} name={}", savedProduct.id, savedProduct.name)
        return savedProduct.toDto()
    }

    @Transactional
    override fun delete(id: Long) {
        log.debug("Deleting product id={}", id)
        val product = productRepository.findRequiredById(id)
        productRepository.delete(product)
        log.info("Deleted product id={}", id)
    }

    private fun ProductRepository.findRequiredById(id: Long): Product =
        findById(id).orElseThrow {
            log.warn("Product id={} not found", id)
            ProductException(id)
        }

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
