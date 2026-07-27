package com.quare.bibleplanner.core.provider.billing.data.datasource

import com.quare.bibleplanner.core.provider.billing.data.dto.OfferingsResponseDto
import com.quare.bibleplanner.core.provider.billing.data.dto.ProductsResponseDto
import com.quare.bibleplanner.core.provider.billing.data.dto.SubscriberResponseDto

internal interface RevenueCatRestDataSource {
    suspend fun getSubscriber(): SubscriberResponseDto

    suspend fun getOfferings(): OfferingsResponseDto

    suspend fun getProducts(productIds: List<String>): ProductsResponseDto
}
