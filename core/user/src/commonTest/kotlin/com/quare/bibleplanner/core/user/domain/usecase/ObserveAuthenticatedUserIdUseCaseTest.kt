package com.quare.bibleplanner.core.user.domain.usecase

import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveAuthenticatedUserIdUseCaseTest {
    private lateinit var sessionStatus: MutableStateFlow<SessionStatus>
    private lateinit var useCase: ObserveAuthenticatedUserIdUseCase

    @Test
    fun `GIVEN an authenticated session WHEN observing THEN emits the user id`() = runTest {
        // Given
        prepareScenario(initialStatus = authenticated(userId = "user-1"))

        // When
        val emissions = mutableListOf<String?>()
        backgroundScope.launch { useCase().collect { emissions += it } }
        runCurrent()

        // Then
        assertEquals(listOf<String?>("user-1"), emissions)
    }

    @Test
    fun `GIVEN no authenticated session WHEN observing THEN emits null`() = runTest {
        // Given
        prepareScenario(initialStatus = SessionStatus.NotAuthenticated())

        // When
        val emissions = mutableListOf<String?>()
        backgroundScope.launch { useCase().collect { emissions += it } }
        runCurrent()

        // Then
        assertEquals(listOf<String?>(null), emissions)
    }

    @Test
    fun `GIVEN the same user re-emitted WHEN observing THEN emits it only once`() = runTest {
        // Given
        prepareScenario(initialStatus = authenticated(userId = "user-1", accessToken = "token-a"))

        // When
        val emissions = mutableListOf<String?>()
        backgroundScope.launch { useCase().collect { emissions += it } }
        runCurrent()
        sessionStatus.value = authenticated(userId = "user-1", accessToken = "token-b")
        runCurrent()

        // Then
        assertEquals(listOf<String?>("user-1"), emissions)
    }

    private fun authenticated(
        userId: String,
        accessToken: String = "",
    ) = SessionStatus.Authenticated(
        session = UserSession(
            accessToken = accessToken,
            refreshToken = "",
            expiresIn = 0,
            tokenType = "",
            user = UserInfo(aud = "", id = userId),
        ),
    )

    private fun prepareScenario(initialStatus: SessionStatus) {
        sessionStatus = MutableStateFlow(initialStatus)
        useCase = ObserveAuthenticatedUserIdUseCase(sessionStatus = sessionStatus)
    }
}
