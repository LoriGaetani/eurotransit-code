package com.example.catalog.error

import com.example.catalog.product.ProductException
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

@RestControllerAdvice
class GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(ProductException::class)
    fun handleProductNotFound(
        exception: ProductException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiError> {
        log.warn("Request failed with not found: method={} path={} message={}", request.method, request.requestURI, exception.message)
        return errorResponse(HttpStatus.NOT_FOUND, exception.message ?: "Product not found", request)
    }

    @ExceptionHandler(
        HttpMessageNotReadableException::class,
        MethodArgumentNotValidException::class,
        MethodArgumentTypeMismatchException::class,
    )
    fun handleBadRequest(
        exception: Exception,
        request: HttpServletRequest,
    ): ResponseEntity<ApiError> {
        log.warn("Bad request: method={} path={} message={}", request.method, request.requestURI, exception.message)
        return errorResponse(HttpStatus.BAD_REQUEST, exception.message ?: "Bad request", request)
    }

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(
        exception: Exception,
        request: HttpServletRequest,
    ): ResponseEntity<ApiError> {
        log.error("Unexpected error: method={} path={}", request.method, request.requestURI, exception)
        return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception.message ?: "Unexpected error", request)
    }

    private fun errorResponse(
        status: HttpStatus,
        message: String,
        request: HttpServletRequest,
    ): ResponseEntity<ApiError> =
        ResponseEntity
            .status(status)
            .body(
                ApiError(
                    status = status.value(),
                    error = status.reasonPhrase,
                    message = message,
                    path = request.requestURI,
                )
            )
}
