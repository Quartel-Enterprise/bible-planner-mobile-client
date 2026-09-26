package com.quare.bibleplanner.core.user.domain.usecase

import com.quare.bibleplanner.core.user.data.mapper.SessionUserMapper
import com.quare.bibleplanner.core.user.domain.model.UserModel
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveCurrentUserUseCaseTest {
    private lateinit var sessionStatus: MutableStateFlow<SessionStatus>
    private lateinit var emissions: List<UserModel?>

    @Test
    fun `GIVEN an authenticated session WHEN observing THEN emits the mapped user`() = runTest {
        // Given
        prepareScenario(initialStatus = authenticated(accessToken = "token-a"))

        // When
        runCurrent()

        // Then
        assertEquals(
            expected = listOf(
                UserModel(
                    id = USER_ID,
                    name = "Ana",
                    email = EMAIL,
                    photo = null,
                    provider = null,
                    lastSignInAt = null,
                    createdAt = null,
                ),
            ),
            actual = emissions,
        )
    }

    @Test
    fun `GIVEN no authenticated session WHEN observing THEN emits no user`() = runTest {
        // Given
        prepareScenario(initialStatus = SessionStatus.NotAuthenticated())

        // When
        runCurrent()

        // Then
        assertEquals(
            expected = listOf<UserModel?>(null),
            actual = emissions,
        )
    }

    @Test
    fun `GIVEN the same user after a token refresh WHEN observing THEN emits it only once`() = runTest {
        // Given
        prepareScenario(initialStatus = authenticated(accessToken = "token-a"))
        runCurrent()

        // When
        sessionStatus.value = authenticated(accessToken = "token-b")
        runCurrent()

        // Then
        assertEquals(
            expected = 1,
            actual = emissions.size,
        )
    }

    private fun authenticated(accessToken: String): SessionStatus = SessionStatus.Authenticated(
        session = UserSession(
            accessToken = accessToken,
            refreshToken = "",
            expiresIn = 0,
            tokenType = "",
            user = UserInfo(
                aud = "",
                id = USER_ID,
                email = EMAIL,
                userMetadata = JsonObject(mapOf("name" to JsonPrimitive("Ana"))),
            ),
        ),
    )

    private fun TestScope.prepareScenario(initialStatus: SessionStatus) {
        sessionStatus = MutableStateFlow(initialStatus)
        val useCase = ObserveCurrentUserUseCase(
            sessionStatus = sessionStatus,
            sessionUserMapper = SessionUserMapper(),
        )
        emissions = mutableListOf<UserModel?>().also { collected ->
            backgroundScope.launch { useCase().collect { collected += it } }
        }
    }

    private companion object {
        const val USER_ID = "user-1"
        const val EMAIL = "ana@example.com"
    }
}
