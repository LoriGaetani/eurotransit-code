package com.example.catalog.product

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
@RequestMapping("/products")
class ProductController(
    private val productService: ProductService,
) {

    @GetMapping
    fun findAll(): List<ProductDto> =
        productService.findAll()

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long): ProductDto =
        productService.findById(id)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody productDto: ProductDto): ProductDto =
        productService.create(productDto)

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody productDto: ProductDto,
    ): ProductDto =
        productService.update(id, productDto)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) {
        productService.delete(id)
    }
}
