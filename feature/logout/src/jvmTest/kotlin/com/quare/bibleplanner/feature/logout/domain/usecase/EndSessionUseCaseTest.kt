package com.quare.bibleplanner.feature.logout.domain.usecase

import com.quare.bibleplanner.core.user.domain.service.IntentionalLogoutMarker
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.MemoryCodeVerifierCache
import io.github.jan.supabase.auth.MemorySessionManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

internal class EndSessionUseCaseTest {
    private lateinit var useCase: EndSessionUseCase
    private lateinit var supabaseClient: SupabaseClient
    private lateinit var logoutMarker: RecordingLogoutMarker
    private var unregisterCalls = 0
    private var clearLocalDataCalls = 0

    @AfterTest
    fun tearDown() = runTest {
        supabaseClient.close()
    }

    @Test
    fun `GIVEN a signed-in device WHEN ending the session THEN unregisters it, signs out and clears local data`() =
        runTest {
            // Given
            prepareScenario(clearLocalData = {})

            // When
            val result = useCase()

            // Then
            assertTrue(result.isSuccess)
            assertEquals(1, unregisterCalls)
            assertEquals(1, clearLocalDataCalls)
            assertIs<SessionStatus.NotAuthenticated>(supabaseClient.auth.sessionStatus.value)
            assertTrue(logoutMarker.isMarked)
        }

    @Test
    fun `GIVEN the device cannot be unregistered WHEN ending the session THEN still signs out`() = runTest {
        // Given
        prepareScenario(
            clearLocalData = {},
            unregisterResult = Result.failure(IllegalStateException("offline")),
        )

        // When
        val result = useCase()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(1, clearLocalDataCalls)
    }

    @Test
    fun `GIVEN clearing local data fails WHEN ending the session THEN unmarks the intentional logout`() = runTest {
        // Given
        prepareScenario(clearLocalData = { error("disk full") })

        // When
        useCase()

        // Then
        assertFalse(logoutMarker.isMarked)
        assertEquals(1, logoutMarker.unmarkCalls)
    }

    private fun prepareScenario(
        clearLocalData: () -> Unit,
        unregisterResult: Result<Unit> = Result.success(Unit),
    ) {
        logoutMarker = RecordingLogoutMarker()
        unregisterCalls = 0
        clearLocalDataCalls = 0
        supabaseClient = createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_KEY,
        ) {
            install(Auth) {
                sessionManager = MemorySessionManager()
                codeVerifierCache = MemoryCodeVerifierCache()
                autoLoadFromStorage = false
                alwaysAutoRefresh = false
            }
            install(Realtime)
        }
        useCase = EndSessionUseCase(
            auth = supabaseClient.auth,
            realtime = supabaseClient.realtime,
            clearLocalUserData = {
                clearLocalDataCalls++
                clearLocalData()
            },
            unregisterCurrentDevice = {
                unregisterCalls++
                unregisterResult
            },
            intentionalLogoutMarker = logoutMarker,
        )
    }

    private companion object {
        const val SUPABASE_URL = "https://project.supabase.co"
        const val SUPABASE_KEY = "anon-key"
    }
}

private class RecordingLogoutMarker : IntentionalLogoutMarker {
    var isMarked = false
        private set
    var unmarkCalls = 0
        private set

    override fun mark() {
        isMarked = true
    }

    override fun unmark() {
        unmarkCalls++
        isMarked = false
    }

    override fun consume(): Boolean = error("unused")
}
