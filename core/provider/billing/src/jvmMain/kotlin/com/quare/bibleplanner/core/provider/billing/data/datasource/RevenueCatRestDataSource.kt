package com.quare.bibleplanner.core.provider.billing.data.datasource

import com.quare.bibleplanner.core.provider.billing.data.dto.OfferingsResponseDto
import com.quare.bibleplanner.core.provider.billing.data.dto.ProductsResponseDto
import com.quare.bibleplanner.core.provider.billing.data.dto.SubscriberResponseDto

internal interface RevenueCatRestDataSource {
    suspend fun getSubscriber(appUserId: String): SubscriberResponseDto

    suspend fun getOfferings(appUserId: String): OfferingsResponseDto

    suspend fun getProducts(
        appUserId: String,
        productIds: List<String>,
    ): ProductsResponseDto
}
