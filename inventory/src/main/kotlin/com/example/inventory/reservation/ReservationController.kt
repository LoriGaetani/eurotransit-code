package com.example.inventory.reservation

import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Validated
@RequestMapping("/api/inventory/reservations")
class ReservationController(
    private val reservationService: ReservationService,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @GetMapping
    fun findAll(): List<ReservationDto> {
        log.info("Received request to list reservations")
        return reservationService.findAll()
    }

    @GetMapping("/{idempotencyKey}")
    fun findByIdempotencyKey(@PathVariable idempotencyKey: String): ReservationDto {
        log.info("Received request to get reservation idempotencyKey={}", idempotencyKey)
        return reservationService.findByIdempotencyKey(idempotencyKey)
    }

    @PostMapping
    fun reserve(@Valid @RequestBody request: ReservationRequest): ResponseEntity<ReservationDto> {
        log.info(
            "Received request to reserve inventory idempotencyKey={} productId={} quantity={}",
            request.idempotencyKey,
            request.productId,
            request.quantity,
        )
        val result = reservationService.reserve(request)
        val status = if (result.created) HttpStatus.CREATED else HttpStatus.OK
        return ResponseEntity.status(status).body(result.reservation)
    }
}
