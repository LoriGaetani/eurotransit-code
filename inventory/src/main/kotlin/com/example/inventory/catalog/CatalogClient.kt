package com.example.inventory.catalog

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestClientResponseException

@Component
class CatalogClient(
    private val catalogRestClient: RestClient,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun assertProductExists(productId: Long) {
        try {
            catalogRestClient
                .get()
                .uri("/api/catalog/products/{productId}", productId)
                .retrieve()
                .toBodilessEntity()
        } catch (exception: RestClientResponseException) {
            if (exception.statusCode.value() == 404) {
                log.warn("Product productId={} not found in catalog", productId)
                throw CatalogProductException(productId)
            }

            log.warn("Catalog returned an error for productId={} status={}", productId, exception.statusCode.value())
            throw CatalogServiceException(productId, exception)
        } catch (exception: RestClientException) {
            log.warn("Catalog request failed for productId={}", productId, exception)
            throw CatalogServiceException(productId, exception)
        }
    }
}
