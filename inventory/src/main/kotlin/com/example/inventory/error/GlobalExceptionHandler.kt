package com.example.inventory.error

import com.example.inventory.catalog.CatalogProductException
import com.example.inventory.catalog.CatalogServiceException
import com.example.inventory.inventory.InventoryItemException
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

    @ExceptionHandler(
        CatalogProductException::class,
        InventoryItemException::class,
    )
    fun handleNotFound(
        exception: RuntimeException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiError> {
        log.warn("Request failed with not found: method={} path={} message={}", request.method, request.requestURI, exception.message)
        return errorResponse(HttpStatus.NOT_FOUND, exception.message ?: "Resource not found", request)
    }

    @ExceptionHandler(CatalogServiceException::class)
    fun handleCatalogUnavailable(
        exception: CatalogServiceException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiError> {
        log.warn("Catalog unavailable: method={} path={} message={}", request.method, request.requestURI, exception.message)
        return errorResponse(HttpStatus.BAD_GATEWAY, exception.message ?: "Catalog unavailable", request)
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
