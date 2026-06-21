package com.example.catalog.product

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/catalog/products")
class ProductController(
    private val productService: ProductService,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @GetMapping
    fun findAll(): List<ProductDto> {
        log.info("Received request to list products")
        return productService.findAll()
    }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long): ProductDto {
        log.info("Received request to get product id={}", id)
        return productService.findById(id)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody productDto: ProductDto): ProductDto {
        log.info("Received request to create product name={} category={}", productDto.name, productDto.category)
        return productService.create(productDto)
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody productDto: ProductDto,
    ): ProductDto {
        log.info("Received request to update product id={}", id)
        return productService.update(id, productDto)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) {
        log.info("Received request to delete product id={}", id)
        productService.delete(id)
    }
}
