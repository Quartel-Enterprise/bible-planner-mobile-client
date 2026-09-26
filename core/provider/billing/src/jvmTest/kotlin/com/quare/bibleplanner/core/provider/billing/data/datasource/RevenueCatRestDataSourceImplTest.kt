package com.quare.bibleplanner.core.provider.billing.data.datasource

import com.quare.bibleplanner.core.provider.billing.data.dto.OfferingsResponseDto
import com.quare.bibleplanner.core.provider.billing.data.dto.ProductsResponseDto
import com.quare.bibleplanner.core.provider.billing.data.dto.TEST_APP_USER_ID
import com.quare.bibleplanner.core.provider.billing.data.dto.freeSubscriberResponse
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.HttpHeaders
import io.ktor.http.Url
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class RevenueCatRestDataSourceImplTest {
    private lateinit var dataSource: RevenueCatRestDataSourceImpl
    private lateinit var requestedUrls: MutableList<Url>

    @Test
    fun `GIVEN a subscriber WHEN fetching it THEN reads the subscriber of the app user`() = runTest {
        // Given
        prepareScenario(responseBody = Json.encodeToString(freeSubscriberResponse()))

        // When
        val subscriber = dataSource.getSubscriber()

        // Then
        assertEquals(
            expected = freeSubscriberResponse(),
            actual = subscriber,
        )
        assertEquals(
            expected = "/v1/subscribers/$TEST_APP_USER_ID",
            actual = requestedUrls.single().encodedPath,
        )
    }

    @Test
    fun `GIVEN offerings WHEN fetching them THEN reads the offerings of the app user`() = runTest {
        // Given
        prepareScenario(responseBody = """{"current_offering_id":"default","offerings":[]}""")

        // When
        val offerings = dataSource.getOfferings()

        // Then
        assertEquals(
            expected = OfferingsResponseDto(
                currentOfferingId = "default",
                offerings = emptyList(),
            ),
            actual = offerings,
        )
        assertEquals(
            expected = "/v1/subscribers/$TEST_APP_USER_ID/offerings",
            actual = requestedUrls.single().encodedPath,
        )
    }

    @Test
    fun `GIVEN product ids WHEN fetching the products THEN asks the web billing api for each of them`() = runTest {
        // Given
        prepareScenario(responseBody = """{"product_details":[]}""")

        // When
        val products = dataSource.getProducts(listOf("prod_monthly", "prod_annual"))

        // Then
        assertEquals(
            expected = ProductsResponseDto(productDetails = emptyList()),
            actual = products,
        )
        val url = requestedUrls.single()
        assertEquals(
            expected = "/rcbilling/v1/subscribers/$TEST_APP_USER_ID/products",
            actual = url.encodedPath,
        )
        assertEquals(
            expected = listOf("prod_monthly", "prod_annual"),
            actual = url.parameters.getAll("id"),
        )
    }

    private fun prepareScenario(responseBody: String) {
        requestedUrls = mutableListOf()
        val engine = MockEngine(
            MockEngineConfig().apply {
                dispatcher = Dispatchers.Unconfined
                addHandler { request ->
                    requestedUrls += request.url
                    respond(
                        content = responseBody,
                        headers = headersOf(
                            name = HttpHeaders.ContentType,
                            value = "application/json",
                        ),
                    )
                }
            },
        )
        val httpClient = HttpClient(engine) {
            defaultRequest { url("https://api.revenuecat.com") }
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }
        dataSource = RevenueCatRestDataSourceImpl(
            httpClient = httpClient,
            getAppUserId = { TEST_APP_USER_ID },
        )
    }
}
