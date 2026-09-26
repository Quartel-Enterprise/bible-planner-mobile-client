package com.quare.bibleplanner.core.provider.supabase

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.MemorySessionManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.storage.Storage
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import kotlin.time.Duration.Companion.seconds

internal class SupabaseClientProviderTest {
    private lateinit var sessionManager: MemorySessionManager
    private lateinit var client: SupabaseClient

    @BeforeTest
    fun setUp() {
        sessionManager = MemorySessionManager()
        client = getSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_KEY,
            googleWebClientId = GOOGLE_WEB_CLIENT_ID,
            monitoredSessionManager = sessionManager,
        )
    }

    @AfterTest
    fun tearDown() = runTest {
        client.close()
    }

    @Test
    fun `GIVEN the project credentials WHEN creating the client THEN points it at the project`() {
        // Then
        assertEquals("project.supabase.co", client.supabaseUrl)
        assertEquals(SUPABASE_KEY, client.supabaseKey)
    }

    @Test
    fun `GIVEN the app client WHEN creating it THEN installs every plugin the app relies on`() {
        // When
        val pluginManager = client.pluginManager

        // Then
        assertNotNull(pluginManager.getPluginOrNull(Auth))
        assertNotNull(pluginManager.getPluginOrNull(ComposeAuth))
        assertNotNull(pluginManager.getPluginOrNull(Storage))
        assertNotNull(pluginManager.getPluginOrNull(Postgrest))
        assertNotNull(pluginManager.getPluginOrNull(Functions))
        assertNotNull(pluginManager.getPluginOrNull(Realtime))
    }

    @Test
    fun `GIVEN the monitored session manager WHEN creating the client THEN auth stores sessions through it`() {
        // When
        val authSessionManager = client.auth.sessionManager

        // Then
        assertSame(sessionManager, authSessionManager)
    }

    @Test
    fun `GIVEN the app client WHEN creating it THEN uses the app timeouts and logs through kermit`() {
        // When
        val config = client.config

        // Then
        assertEquals(30.seconds, config.networkConfig.requestTimeout)
        assertEquals(10.seconds, client.realtime.config.heartbeatInterval)
        assertEquals(3.seconds, client.realtime.config.reconnectDelay)
        assertIs<KermitSupabaseLoggingProcessor>(
            config.loggingConfig.defaultLoggingFactory(config.loggingConfig.defaultLogLevel),
        )
    }

    private companion object {
        const val SUPABASE_URL = "https://project.supabase.co"
        const val SUPABASE_KEY = "anon-key"
        const val GOOGLE_WEB_CLIENT_ID = "google-client-id"
    }
}
