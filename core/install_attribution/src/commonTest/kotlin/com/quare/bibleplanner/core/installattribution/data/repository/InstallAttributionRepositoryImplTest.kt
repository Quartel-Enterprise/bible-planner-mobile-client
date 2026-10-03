package com.quare.bibleplanner.core.installattribution.data.repository

import androidx.datastore.preferences.core.emptyPreferences
import com.quare.bibleplanner.core.installattribution.data.datasource.InstallAttributionLocalDataSource
import com.quare.bibleplanner.core.installattribution.data.datasource.InstallAttributionRemoteDataSource
import com.quare.bibleplanner.core.installattribution.data.dto.TrackAppInstallRequestDto
import com.quare.bibleplanner.core.installattribution.domain.model.InstallReport
import com.quare.bibleplanner.core.installattribution.domain.model.InstallReportOutcome
import com.quare.bibleplanner.core.provider.datastore.testing.FakePreferencesDataStore
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.functions.functions
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InstallAttributionRepositoryImplTest {
    private lateinit var repository: InstallAttributionRepositoryImpl
    private lateinit var requestedBodies: MutableList<String>

    @Test
    fun `GIVEN an install report WHEN sending it THEN maps it to the request body`() = runTest {
        // Given
        prepareScenario()

        // When
        val outcome = repository.sendInstallReport(
            InstallReport(
                installId = "install-1",
                platform = "android",
                oppref = "click-1",
                advertisingId = "gaid-1",
            ),
        )

        // Then
        assertEquals(
            expected = InstallReportOutcome.DELIVERED,
            actual = outcome,
        )
        assertEquals(
            expected = TrackAppInstallRequestDto(
                installId = "install-1",
                platform = "android",
                oppref = "click-1",
                gaid = "gaid-1",
            ),
            actual = Json.decodeFromString<TrackAppInstallRequestDto>(requestedBodies.single()),
        )
    }

    @Test
    fun `GIVEN a fresh install WHEN marking it reported THEN is reported`() = runTest {
        // Given
        prepareScenario()
        assertFalse(repository.isInstallReported())

        // When
        repository.markInstallReported()

        // Then
        assertTrue(repository.isInstallReported())
    }

    @Test
    fun `GIVEN a fresh install WHEN reading the install id twice THEN keeps the same id`() = runTest {
        // Given
        prepareScenario()

        // When
        val first = repository.getOrCreateInstallId()
        val second = repository.getOrCreateInstallId()

        // Then
        assertEquals(
            expected = first,
            actual = second,
        )
    }

    private fun prepareScenario() {
        requestedBodies = mutableListOf()
        val client = createSupabaseClient(
            supabaseUrl = "https://project.supabase.co",
            supabaseKey = "publishable-key",
        ) {
            httpEngine = MockEngine(
                MockEngineConfig().apply {
                    dispatcher = Dispatchers.Unconfined
                    addHandler { httpRequest ->
                        requestedBodies += httpRequest.body.toByteArray().decodeToString()
                        respond(content = """{"ok":true}""", status = HttpStatusCode.OK)
                    }
                },
            )
            install(Functions)
        }
        repository = InstallAttributionRepositoryImpl(
            localDataSource = InstallAttributionLocalDataSource(FakePreferencesDataStore(emptyPreferences())),
            remoteDataSource = InstallAttributionRemoteDataSource(
                functions = client.functions,
                json = Json,
            ),
        )
    }
}
