package com.example.inventory.catalog

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class CatalogClientConfig {
    @Bean
    fun catalogRestClient(
        @Value("\${clients.catalog.base-url}") catalogBaseUrl: String,
    ): RestClient =
        RestClient
            .builder()
            .baseUrl(catalogBaseUrl)
            .build()
}
