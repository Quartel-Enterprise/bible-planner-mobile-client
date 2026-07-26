package com.quare.bibleplanner.core.provider.billing.data.datasource

import com.quare.bibleplanner.core.provider.billing.data.dto.OfferingsResponseDto
import com.quare.bibleplanner.core.provider.billing.data.dto.ProductsResponseDto
import com.quare.bibleplanner.core.provider.billing.data.dto.SubscriberResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.appendPathSegments

internal class RevenueCatRestDataSourceImpl(
    private val httpClient: HttpClient,
) : RevenueCatRestDataSource {
    override suspend fun getSubscriber(appUserId: String): SubscriberResponseDto = httpClient
        .get {
            url {
                appendPathSegments(
                    SUBSCRIBERS_PATH,
                    appUserId,
                )
            }
        }.body()

    override suspend fun getOfferings(appUserId: String): OfferingsResponseDto = httpClient
        .get {
            url {
                appendPathSegments(
                    SUBSCRIBERS_PATH,
                    appUserId,
                    OFFERINGS_SEGMENT,
                )
            }
        }.body()

    override suspend fun getProducts(
        appUserId: String,
        productIds: List<String>,
    ): ProductsResponseDto = httpClient
        .get {
            url {
                appendPathSegments(
                    WEB_BILLING_SUBSCRIBERS_PATH,
                    appUserId,
                    PRODUCTS_SEGMENT,
                )
                productIds.forEach { productId ->
                    parameters.append(
                        name = PRODUCT_ID_PARAM,
                        value = productId,
                    )
                }
            }
        }.body()

    private companion object {
        const val SUBSCRIBERS_PATH = "v1/subscribers"
        const val WEB_BILLING_SUBSCRIBERS_PATH = "rcbilling/v1/subscribers"
        const val OFFERINGS_SEGMENT = "offerings"
        const val PRODUCTS_SEGMENT = "products"
        const val PRODUCT_ID_PARAM = "id"
    }
}
