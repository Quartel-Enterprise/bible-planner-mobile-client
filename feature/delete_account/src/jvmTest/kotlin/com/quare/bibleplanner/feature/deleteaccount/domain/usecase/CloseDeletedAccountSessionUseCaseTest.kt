package com.quare.bibleplanner.feature.deleteaccount.domain.usecase

import com.quare.bibleplanner.core.provider.supabase.session.ExpectedSessionDeletion
import com.quare.bibleplanner.core.user.domain.service.IntentionalLogoutMarker
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.MemoryCodeVerifierCache
import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.NoSessionFoundException
import io.github.jan.supabase.auth.user.UserSession
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

internal class CloseDeletedAccountSessionUseCaseTest {
    private lateinit var useCase: CloseDeletedAccountSessionUseCase
    private lateinit var supabaseClient: SupabaseClient
    private lateinit var sessionManager: RecordingSessionManager
    private lateinit var logoutMarker: RecordingLogoutMarker
    private lateinit var expectedSessionDeletion: ExpectedSessionDeletion
    private var clearLocalDataCalls = 0

    @AfterTest
    fun tearDown() = runTest {
        supabaseClient.close()
    }

    @Test
    fun `GIVEN a deleted account WHEN closing its session THEN marks the logout and clears local data`() = runTest {
        // Given
        prepareScenario(clearLocalData = {})

        // When
        val result = useCase()

        // Then
        assertTrue(result.isSuccess)
        assertTrue(logoutMarker.isMarked)
        assertEquals(1, clearLocalDataCalls)
    }

    @Test
    fun `GIVEN a deleted account WHEN closing its session THEN deletes the stored session as an expected deletion`() =
        runTest {
            // Given
            prepareScenario(clearLocalData = {})

            // When
            useCase()

            // Then
            assertEquals(listOf(true), sessionManager.deletionsWhileExpected)
            assertFalse(expectedSessionDeletion.isExpected)
        }

    @Test
    fun `GIVEN clearing local data fails WHEN closing the session THEN returns the failure`() = runTest {
        // Given
        prepareScenario(clearLocalData = { error("disk full") })

        // When
        val result = useCase()

        // Then
        val error = assertIs<IllegalStateException>(result.exceptionOrNull())
        assertEquals("disk full", error.message)
        assertTrue(logoutMarker.isMarked)
    }

    private fun prepareScenario(clearLocalData: () -> Unit) {
        expectedSessionDeletion = ExpectedSessionDeletion()
        sessionManager = RecordingSessionManager(expectedSessionDeletion)
        logoutMarker = RecordingLogoutMarker()
        clearLocalDataCalls = 0
        supabaseClient = createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_KEY,
        ) {
            install(Auth) {
                sessionManager = this@CloseDeletedAccountSessionUseCaseTest.sessionManager
                codeVerifierCache = MemoryCodeVerifierCache()
                autoLoadFromStorage = false
                alwaysAutoRefresh = false
            }
            install(Realtime)
        }
        useCase = CloseDeletedAccountSessionUseCase(
            auth = supabaseClient.auth,
            realtime = supabaseClient.realtime,
            clearLocalUserData = {
                clearLocalDataCalls++
                clearLocalData()
            },
            intentionalLogoutMarker = logoutMarker,
            expectedSessionDeletion = expectedSessionDeletion,
        )
    }

    private companion object {
        const val SUPABASE_URL = "https://project.supabase.co"
        const val SUPABASE_KEY = "anon-key"
    }
}

private class RecordingSessionManager(
    private val expectedSessionDeletion: ExpectedSessionDeletion,
) : SessionManager {
    val deletionsWhileExpected = mutableListOf<Boolean>()

    override suspend fun saveSession(session: UserSession) = error("unused")

    override suspend fun loadSession(): UserSession = throw NoSessionFoundException()

    override suspend fun deleteSession() {
        deletionsWhileExpected += expectedSessionDeletion.isExpected
    }
}

private class RecordingLogoutMarker : IntentionalLogoutMarker {
    var isMarked = false
        private set

    override fun mark() {
        isMarked = true
    }

    override fun unmark() {
        isMarked = false
    }

    override fun consume(): Boolean = error("unused")
}
