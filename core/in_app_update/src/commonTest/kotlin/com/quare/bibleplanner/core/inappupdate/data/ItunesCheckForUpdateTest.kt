package com.quare.bibleplanner.core.inappupdate.data

import com.quare.bibleplanner.core.inappupdate.domain.model.UpdateAvailability
import com.quare.bibleplanner.core.inappupdate.generated.InAppUpdateBuildKonfig
import com.quare.bibleplanner.core.network.data.handler.RequestHandler
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

internal class ItunesCheckForUpdateTest {
    private lateinit var checkForUpdate: ItunesCheckForUpdate
    private lateinit var requestedUrls: MutableList<Url>

    @Test
    fun `GIVEN a newer version in the store WHEN checking THEN an update is available`() = runTest {
        // Given
        prepareScenario(respond = { lookupBody(NEWER_VERSION) })

        // When
        val availability = checkForUpdate()

        // Then
        assertEquals(UpdateAvailability.Available(versionName = NEWER_VERSION), availability)
    }

    @Test
    fun `GIVEN the installed version in the store WHEN checking THEN no update is available`() = runTest {
        // Given
        prepareScenario(respond = { lookupBody(InAppUpdateBuildKonfig.APP_VERSION) })

        // When
        val availability = checkForUpdate()

        // Then
        assertEquals(UpdateAvailability.NotAvailable, availability)
    }

    @Test
    fun `GIVEN the store does not list the app WHEN checking THEN no update is available`() = runTest {
        // Given
        prepareScenario(respond = { EMPTY_LOOKUP_BODY })

        // When
        val availability = checkForUpdate()

        // Then
        assertEquals(UpdateAvailability.NotAvailable, availability)
    }

    @Test
    fun `GIVEN the lookup fails WHEN checking THEN the check failed instead of reporting up to date`() = runTest {
        // Given
        prepareScenario(respond = { null })

        // When
        val availability = checkForUpdate()

        // Then
        assertEquals(UpdateAvailability.CheckFailed, availability)
    }

    @Test
    fun `GIVEN two checks WHEN looking up THEN each one asks for a different URL so no cache can answer it`() =
        runTest {
            // Given
            prepareScenario(respond = { lookupBody(NEWER_VERSION) })

            // When
            checkForUpdate()
            checkForUpdate()

            // Then
            val cacheBusters = requestedUrls.map { it.parameters[CACHE_BUSTER_PARAMETER] }
            assertEquals(2, cacheBusters.filterNotNull().size)
            assertNotEquals(cacheBusters[0], cacheBusters[1])
        }

    @Test
    fun `GIVEN a device region WHEN checking THEN looks up that region's store`() = runTest {
        // Given
        prepareScenario(
            regionCode = REGION_CODE,
            respond = { lookupBody(NEWER_VERSION) },
        )

        // When
        checkForUpdate()

        // Then
        assertEquals(listOf<String?>(REGION_CODE), requestedUrls.map { it.parameters[COUNTRY_PARAMETER] })
    }

    @Test
    fun `GIVEN no device region WHEN checking THEN looks up the default store`() = runTest {
        // Given
        prepareScenario(respond = { lookupBody(NEWER_VERSION) })

        // When
        checkForUpdate()

        // Then
        assertNull(requestedUrls.single().parameters[COUNTRY_PARAMETER])
    }

    @Test
    fun `GIVEN the region's store does not list the app WHEN checking THEN falls back to the default store`() =
        runTest {
            // Given
            prepareScenario(
                regionCode = REGION_CODE,
                respond = { url ->
                    if (url.parameters[COUNTRY_PARAMETER] == null) lookupBody(NEWER_VERSION) else EMPTY_LOOKUP_BODY
                },
            )

            // When
            val availability = checkForUpdate()

            // Then
            assertEquals(UpdateAvailability.Available(versionName = NEWER_VERSION), availability)
            assertEquals(
                expected = listOf(REGION_CODE, null),
                actual = requestedUrls.map { it.parameters[COUNTRY_PARAMETER] },
            )
        }

    @Test
    fun `GIVEN the region's lookup fails WHEN checking THEN the check failed without retrying`() = runTest {
        // Given
        prepareScenario(
            regionCode = REGION_CODE,
            respond = { null },
        )

        // When
        val availability = checkForUpdate()

        // Then
        assertEquals(UpdateAvailability.CheckFailed, availability)
        assertEquals(1, requestedUrls.size)
    }

    private fun lookupBody(version: String): String = """{"resultCount":1,"results":[{"version":"$version"}]}"""

    private fun prepareScenario(
        regionCode: String? = null,
        respond: (Url) -> String?,
    ) {
        requestedUrls = mutableListOf()
        val engine = MockEngine { request ->
            requestedUrls += request.url
            val body = respond(request.url)
            respond(
                content = body.orEmpty(),
                status = if (body == null) HttpStatusCode.ServiceUnavailable else HttpStatusCode.OK,
                headers = headersOf(
                    name = HttpHeaders.ContentType,
                    value = ContentType.Text.JavaScript.toString(),
                ),
            )
        }
        val httpClient = HttpClient(engine) {
            install(ContentNegotiation) {
                json(
                    json = Json { ignoreUnknownKeys = true },
                    contentType = ContentType.Text.JavaScript,
                )
            }
        }
        checkForUpdate = ItunesCheckForUpdate(
            requestHandler = RequestHandler(httpClient),
            deviceRegionProvider = { regionCode },
        )
    }

    private companion object {
        const val NEWER_VERSION = "999.0.0"
        const val REGION_CODE = "BR"
        const val COUNTRY_PARAMETER = "country"
        const val CACHE_BUSTER_PARAMETER = "t"
        const val EMPTY_LOOKUP_BODY = """{"resultCount":0,"results":[]}"""
    }
}
