package com.quare.bibleplanner.feature.logout.domain.usecase

import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.user.domain.service.IntentionalLogoutMarker
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.status.RefreshFailureCause
import io.github.jan.supabase.auth.status.SessionSource
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class ObserveSessionLossUseCaseTest {
    private lateinit var sessionStatus: MutableStateFlow<SessionStatus>
    private lateinit var intentionalLogoutMarker: IntentionalLogoutMarker
    private lateinit var trackedEvents: MutableList<Pair<String, Map<String, Any>>>
    private lateinit var useCase: ObserveSessionLossUseCase

    @BeforeTest
    fun setUp() {
        sessionStatus = MutableStateFlow(SessionStatus.Initializing)
        intentionalLogoutMarker = FakeIntentionalLogoutMarker()
        trackedEvents = mutableListOf()
        useCase = ObserveSessionLossUseCase(
            sessionStatus = sessionStatus,
            intentionalLogoutMarker = intentionalLogoutMarker,
            currentTimestampProvider = CurrentTimestampProvider { NOW_MILLIS },
            trackEvent = TrackEvent { name, params -> trackedEvents += name to params },
        )
    }

    @Test
    fun `GIVEN an authenticated session WHEN it becomes not authenticated without a logout THEN tracks session_lost`() =
        runTest {
            // Given
            backgroundScope.launch { useCase() }
            sessionStatus.value = authenticated(expiresAtMillis = NOW_MILLIS + HOUR_MILLIS)
            runCurrent()

            // When
            sessionStatus.value = SessionStatus.NotAuthenticated()
            runCurrent()

            // Then
            assertEquals(
                expected = listOf(
                    AnalyticsEventNames.SESSION_LOST to mapOf<String, Any>(
                        AnalyticsParams.SOURCE to "sign_in",
                        AnalyticsParams.IS_ACCESS_TOKEN_EXPIRED to false,
                    ),
                ),
                actual = trackedEvents,
            )
        }

    @Test
    fun `GIVEN an expired access token WHEN the session is lost THEN tracks the expiry param as true`() = runTest {
        // Given
        backgroundScope.launch { useCase() }
        sessionStatus.value = authenticated(expiresAtMillis = NOW_MILLIS - 1)
        runCurrent()

        // When
        sessionStatus.value = SessionStatus.NotAuthenticated()
        runCurrent()

        // Then
        assertEquals(
            expected = true,
            actual = trackedEvents.single().second[AnalyticsParams.IS_ACCESS_TOKEN_EXPIRED],
        )
    }

    @Test
    fun `GIVEN an intentional logout WHEN the session ends THEN tracks nothing`() = runTest {
        // Given
        backgroundScope.launch { useCase() }
        sessionStatus.value = authenticated(expiresAtMillis = NOW_MILLIS + HOUR_MILLIS)
        runCurrent()
        intentionalLogoutMarker.mark()

        // When
        sessionStatus.value = SessionStatus.NotAuthenticated(isSignOut = true)
        runCurrent()

        // Then
        assertEquals(
            expected = emptyList(),
            actual = trackedEvents,
        )
    }

    @Test
    fun `GIVEN no previous authenticated session WHEN starting logged out THEN tracks nothing`() = runTest {
        // Given
        backgroundScope.launch { useCase() }
        runCurrent()

        // When
        sessionStatus.value = SessionStatus.NotAuthenticated()
        runCurrent()

        // Then
        assertEquals(
            expected = emptyList(),
            actual = trackedEvents,
        )
    }

    @Test
    fun `GIVEN a refresh failure in between WHEN the session is lost THEN still tracks session_lost`() = runTest {
        // Given
        backgroundScope.launch { useCase() }
        sessionStatus.value = authenticated(expiresAtMillis = NOW_MILLIS - 1)
        runCurrent()
        sessionStatus.value = SessionStatus.RefreshFailure(FAKE_REFRESH_FAILURE_CAUSE)
        runCurrent()

        // When
        sessionStatus.value = SessionStatus.NotAuthenticated()
        runCurrent()

        // Then
        assertEquals(
            expected = AnalyticsEventNames.SESSION_LOST,
            actual = trackedEvents.single().first,
        )
    }

    @Test
    fun `GIVEN a previously reported loss WHEN the user is still logged out THEN does not track again`() = runTest {
        // Given
        backgroundScope.launch { useCase() }
        sessionStatus.value = authenticated(expiresAtMillis = NOW_MILLIS + HOUR_MILLIS)
        runCurrent()
        sessionStatus.value = SessionStatus.NotAuthenticated()
        runCurrent()

        // When
        sessionStatus.value = SessionStatus.NotAuthenticated(isSignOut = true)
        runCurrent()

        // Then
        assertEquals(
            expected = 1,
            actual = trackedEvents.size,
        )
    }

    private fun authenticated(expiresAtMillis: Long) = SessionStatus.Authenticated(
        session = UserSession(
            accessToken = "access-token",
            refreshToken = "refresh-token",
            expiresIn = 3600,
            tokenType = "bearer",
            user = UserInfo(aud = "", id = "user-1"),
            expiresAt = Instant.fromEpochMilliseconds(expiresAtMillis),
        ),
        source = SessionSource.SignIn(Google),
    )

    private class FakeIntentionalLogoutMarker : IntentionalLogoutMarker {
        private var isMarked = false

        override fun mark() {
            isMarked = true
        }

        override fun unmark() {
            isMarked = false
        }

        override fun consume(): Boolean {
            val wasMarked = isMarked
            isMarked = false
            return wasMarked
        }
    }

    private companion object {
        const val NOW_MILLIS = 1_000_000L
        const val HOUR_MILLIS = 3_600_000L
        val FAKE_REFRESH_FAILURE_CAUSE = RefreshFailureCause.NetworkError(Exception("offline"))
    }
}
