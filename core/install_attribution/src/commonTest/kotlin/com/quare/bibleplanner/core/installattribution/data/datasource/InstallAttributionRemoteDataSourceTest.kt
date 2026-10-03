package com.quare.bibleplanner.core.installattribution.data.datasource

import com.quare.bibleplanner.core.installattribution.data.dto.TrackAppInstallRequestDto
import com.quare.bibleplanner.core.installattribution.domain.model.InstallReportOutcome
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.functions.functions
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.engine.mock.toByteArray
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class InstallAttributionRemoteDataSourceTest {
    private val request = TrackAppInstallRequestDto(
        installId = "install-1",
        platform = "android",
        oppref = "click-1",
        gaid = null,
    )
    private lateinit var dataSource: InstallAttributionRemoteDataSource
    private lateinit var requestedPaths: MutableList<String>
    private lateinit var requestedBodies: MutableList<String>

    @Test
    fun `GIVEN an install WHEN tracking it THEN posts it to the track function`() = runTest {
        // Given
        prepareScenario(status = HttpStatusCode.OK)

        // When
        dataSource.trackAppInstall(request)

        // Then
        assertEquals(
            expected = listOf("/functions/v1/track-app-install"),
            actual = requestedPaths,
        )
        assertEquals(
            expected = Json.parseToJsonElement(
                """{"install_id":"install-1","platform":"android","oppref":"click-1","gaid":null}""",
            ),
            actual = Json.parseToJsonElement(requestedBodies.single()),
        )
    }

    @Test
    fun `GIVEN a skipped install WHEN tracking it THEN is delivered`() = runTest {
        // Given
        prepareScenario(status = HttpStatusCode.Accepted)

        // When
        val outcome = dataSource.trackAppInstall(request)

        // Then
        assertEquals(
            expected = InstallReportOutcome.DELIVERED,
            actual = outcome,
        )
    }

    @Test
    fun `GIVEN a bad request WHEN tracking it THEN is rejected`() = runTest {
        // Given
        prepareScenario(status = HttpStatusCode.BadRequest)

        // When
        val outcome = dataSource.trackAppInstall(request)

        // Then
        assertEquals(
            expected = InstallReportOutcome.REJECTED,
            actual = outcome,
        )
    }

    @Test
    fun `GIVEN an unprocessable request WHEN tracking it THEN is rejected`() = runTest {
        // Given
        prepareScenario(status = HttpStatusCode.UnprocessableEntity)

        // When
        val outcome = dataSource.trackAppInstall(request)

        // Then
        assertEquals(
            expected = InstallReportOutcome.REJECTED,
            actual = outcome,
        )
    }

    @Test
    fun `GIVEN a server error WHEN tracking it THEN fails so it is retried`() = runTest {
        // Given
        prepareScenario(status = HttpStatusCode.InternalServerError)

        // When
        val outcome = dataSource.trackAppInstall(request)

        // Then
        assertEquals(
            expected = InstallReportOutcome.FAILED,
            actual = outcome,
        )
    }

    @Test
    fun `GIVEN a network failure WHEN tracking it THEN fails so it is retried`() = runTest {
        // Given
        prepareScenario(status = null)

        // When
        val outcome = dataSource.trackAppInstall(request)

        // Then
        assertEquals(
            expected = InstallReportOutcome.FAILED,
            actual = outcome,
        )
    }

    private fun prepareScenario(status: HttpStatusCode?) {
        requestedPaths = mutableListOf()
        requestedBodies = mutableListOf()
        val client = createSupabaseClient(
            supabaseUrl = "https://project.supabase.co",
            supabaseKey = "publishable-key",
        ) {
            httpEngine = MockEngine(
                MockEngineConfig().apply {
                    dispatcher = Dispatchers.Unconfined
                    addHandler { httpRequest ->
                        requestedPaths += httpRequest.url.encodedPath
                        requestedBodies += httpRequest.body.toByteArray().decodeToString()
                        if (status == null) {
                            error("network down")
                        } else if (status.value >= HttpStatusCode.BadRequest.value) {
                            respondError(status = status, content = """{"error":"nope"}""")
                        } else {
                            respond(
                                content = """{"ok":true}""",
                                status = status,
                                headers = headersOf(HttpHeaders.ContentType, "application/json"),
                            )
                        }
                    }
                },
            )
            install(Functions)
        }
        dataSource = InstallAttributionRemoteDataSource(
            functions = client.functions,
            json = Json,
        )
    }
}
