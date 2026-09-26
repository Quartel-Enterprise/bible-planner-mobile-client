package com.quare.bibleplanner.core.provider.supabase

import com.quare.bibleplanner.core.provider.supabase.session.createPlatformSessionManager
import io.github.jan.supabase.auth.AuthConfig
import io.github.jan.supabase.auth.SettingsSessionManager
import io.ktor.client.engine.cio.CIOEngineConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class SupabasePlatformTest {
    @Test
    fun `GIVEN the desktop app WHEN configuring auth THEN titles the OAuth callback page with the app name`() {
        // Given
        val authConfig = AuthConfig()

        // When
        authConfig.platformConfig()

        // Then
        assertEquals("Bible Planner", authConfig.httpCallbackConfig.htmlTitle)
    }

    @Test
    fun `GIVEN the desktop app WHEN creating the http engine THEN uses the CIO engine`() {
        // When
        val engine = createPlatformHttpEngine()

        // Then
        assertIs<CIOEngineConfig>(engine.config)
        engine.close()
    }

    @Test
    fun `GIVEN the desktop app WHEN creating the session storage THEN keeps sessions in the platform settings`() {
        // When
        val sessionManager = createPlatformSessionManager()

        // Then
        assertIs<SettingsSessionManager>(sessionManager)
    }
}
