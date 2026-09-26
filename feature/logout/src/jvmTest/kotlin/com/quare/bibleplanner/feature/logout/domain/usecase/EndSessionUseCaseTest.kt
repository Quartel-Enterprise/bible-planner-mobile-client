package com.quare.bibleplanner.feature.logout.domain.usecase

import com.quare.bibleplanner.core.user.domain.service.IntentionalLogoutMarker
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.MemoryCodeVerifierCache
import io.github.jan.supabase.auth.MemorySessionManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.realtime
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.Dispatchers
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

    @Test
    fun `GIVEN clearing local data fails WHEN ending the session THEN returns the failure`() = runTest {
        // Given
        prepareScenario(clearLocalData = { error("disk full") })

        // When
        val result = useCase()

        // Then
        assertIs<IllegalStateException>(result.exceptionOrNull())
    }

    @Test
    fun `GIVEN the sign-out request fails WHEN ending the session THEN returns the failure and keeps local data`() =
        runTest {
            // Given
            prepareScenario(
                clearLocalData = {},
                signOutFails = true,
            )

            // When
            val result = useCase()

            // Then
            assertTrue(result.isFailure)
            assertEquals(0, clearLocalDataCalls)
            assertFalse(logoutMarker.isMarked)
        }

    private suspend fun prepareScenario(
        clearLocalData: () -> Unit,
        unregisterResult: Result<Unit> = Result.success(Unit),
        signOutFails: Boolean = false,
    ) {
        logoutMarker = RecordingLogoutMarker()
        unregisterCalls = 0
        clearLocalDataCalls = 0
        supabaseClient = createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_KEY,
        ) {
            httpEngine = MockEngine(
                MockEngineConfig().apply {
                    dispatcher = Dispatchers.Unconfined
                    addHandler {
                        respond(
                            content = SERVER_ERROR,
                            status = HttpStatusCode.InternalServerError,
                            headers = headersOf(
                                name = HttpHeaders.ContentType,
                                value = "application/json",
                            ),
                        )
                    }
                },
            )
            install(Auth) {
                sessionManager = MemorySessionManager()
                codeVerifierCache = MemoryCodeVerifierCache()
                autoLoadFromStorage = false
                alwaysAutoRefresh = false
            }
            install(Realtime)
        }
        if (signOutFails) {
            supabaseClient.auth.importSession(
                session = UserSession(
                    accessToken = "access-token",
                    refreshToken = "refresh-token",
                    expiresIn = 3600,
                    tokenType = "bearer",
                    user = UserInfo(
                        aud = "authenticated",
                        id = "user-1",
                    ),
                ),
                autoRefresh = false,
            )
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
        const val SERVER_ERROR = """{"code":500,"msg":"Internal error"}"""
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
