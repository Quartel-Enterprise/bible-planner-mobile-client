package com.quare.bibleplanner.core.user.domain.usecase.impl

import com.quare.bibleplanner.core.user.domain.model.RemoteSessionState
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.MemoryCodeVerifierCache
import io.github.jan.supabase.auth.MemorySessionManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.createSupabaseClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CheckRemoteSessionStateUseCaseTest {
    private lateinit var useCase: CheckRemoteSessionStateUseCase

    @Test
    fun `GIVEN the refresh succeeds WHEN checking THEN the session is active`() = runTest {
        // Given
        prepareScenario(
            refreshStatus = HttpStatusCode.OK,
            refreshBody = REFRESHED_SESSION,
        )

        // When
        val state = useCase()

        // Then
        assertEquals(
            expected = RemoteSessionState.ACTIVE,
            actual = state,
        )
    }

    @Test
    fun `GIVEN the server rejects the refresh token WHEN checking THEN the session was revoked`() = runTest {
        // Given
        prepareScenario(
            refreshStatus = HttpStatusCode.BadRequest,
            refreshBody = SESSION_NOT_FOUND,
        )

        // When
        val state = useCase()

        // Then
        assertEquals(
            expected = RemoteSessionState.REVOKED,
            actual = state,
        )
    }

    @Test
    fun `GIVEN a server error WHEN checking THEN the state is unknown`() = runTest {
        // Given
        prepareScenario(
            refreshStatus = HttpStatusCode.InternalServerError,
            refreshBody = SERVER_ERROR,
        )

        // When
        val state = useCase()

        // Then
        assertEquals(
            expected = RemoteSessionState.UNKNOWN,
            actual = state,
        )
    }

    @Test
    fun `GIVEN no local session WHEN checking THEN the state is unknown`() = runTest {
        // Given
        prepareScenario(
            refreshStatus = HttpStatusCode.OK,
            refreshBody = REFRESHED_SESSION,
            hasLocalSession = false,
        )

        // When
        val state = useCase()

        // Then
        assertEquals(
            expected = RemoteSessionState.UNKNOWN,
            actual = state,
        )
    }

    private suspend fun prepareScenario(
        refreshStatus: HttpStatusCode,
        refreshBody: String,
        hasLocalSession: Boolean = true,
    ) {
        val client = createSupabaseClient(
            supabaseUrl = "https://project.supabase.co",
            supabaseKey = "anon-key",
        ) {
            httpEngine = MockEngine(
                MockEngineConfig().apply {
                    dispatcher = Dispatchers.Unconfined
                    addHandler {
                        respond(
                            content = refreshBody,
                            status = refreshStatus,
                            headers = headersOf(
                                name = HttpHeaders.ContentType,
                                value = "application/json",
                            ),
                        )
                    }
                },
            )
            install(Auth) {
                alwaysAutoRefresh = false
                autoLoadFromStorage = false
                autoSaveToStorage = false
                enableLifecycleCallbacks = false
                sessionManager = MemorySessionManager()
                codeVerifierCache = MemoryCodeVerifierCache()
            }
        }
        if (hasLocalSession) {
            client.auth.importSession(
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
        useCase = CheckRemoteSessionStateUseCase(client.auth)
    }

    private companion object {
        const val REFRESHED_SESSION =
            """{"access_token":"new-token","refresh_token":"new-refresh","expires_in":3600,"token_type":"bearer",""" +
                """"user":{"id":"user-1","aud":"authenticated"}}"""
        const val SESSION_NOT_FOUND =
            """{"code":400,"error_code":"refresh_token_not_found","msg":"Invalid Refresh Token"}"""
        const val SERVER_ERROR = """{"code":500,"msg":"Internal error"}"""
    }
}
