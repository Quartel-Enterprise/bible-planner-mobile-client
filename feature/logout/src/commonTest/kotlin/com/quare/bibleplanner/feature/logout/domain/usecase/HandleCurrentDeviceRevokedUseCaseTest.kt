package com.quare.bibleplanner.feature.logout.domain.usecase

import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.user.domain.model.RemoteSessionState
import com.quare.bibleplanner.core.user.domain.service.IntentionalLogoutMarker
import com.quare.bibleplanner.core.user.domain.usecase.CheckRemoteSessionState
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HandleCurrentDeviceRevokedUseCaseTest {
    private lateinit var remoteState: RemoteSessionState
    private lateinit var marker: RecordingIntentionalLogoutMarker
    private lateinit var trackedEvents: MutableList<Pair<String, Map<String, Any>>>
    private lateinit var order: MutableList<String>
    private lateinit var useCase: HandleCurrentDeviceRevokedUseCase

    @BeforeTest
    fun setUp() {
        remoteState = RemoteSessionState.REVOKED
        trackedEvents = mutableListOf()
        order = mutableListOf()
        marker = RecordingIntentionalLogoutMarker(order)
        useCase = HandleCurrentDeviceRevokedUseCase(
            checkRemoteSessionState = CheckRemoteSessionState { remoteState },
            intentionalLogoutMarker = marker,
            trackEvent = TrackEvent { name, params -> trackedEvents += name to params },
            endSession = {
                order += "endSession"
                Result.success(Unit)
            },
        )
    }

    @Test
    fun `GIVEN the server reports the session revoked WHEN invoked THEN tracks state revoked`() = runTest {
        // Given
        remoteState = RemoteSessionState.REVOKED

        // When
        useCase()

        // Then
        assertEquals(
            expected = AnalyticsEventNames.CURRENT_DEVICE_REVOKED to mapOf<String, Any>(
                AnalyticsParams.SERVER_SESSION_STATE to "revoked",
            ),
            actual = trackedEvents.single(),
        )
    }

    @Test
    fun `GIVEN the server reports the session still active WHEN invoked THEN tracks state active`() = runTest {
        // Given
        remoteState = RemoteSessionState.ACTIVE

        // When
        useCase()

        // Then
        assertEquals(
            expected = "active",
            actual = trackedEvents.single().second[AnalyticsParams.SERVER_SESSION_STATE],
        )
    }

    @Test
    fun `GIVEN an inconclusive probe WHEN invoked THEN tracks state unknown`() = runTest {
        // Given
        remoteState = RemoteSessionState.UNKNOWN

        // When
        useCase()

        // Then
        assertEquals(
            expected = "unknown",
            actual = trackedEvents.single().second[AnalyticsParams.SERVER_SESSION_STATE],
        )
    }

    @Test
    fun `WHEN invoked THEN marks the logout as intentional before ending the session`() = runTest {
        // When
        useCase()

        // Then
        assertEquals(
            expected = listOf("mark", "endSession"),
            actual = order,
        )
    }

    @Test
    fun `WHEN invoked THEN ends the local session`() = runTest {
        // When
        useCase()

        // Then
        assertTrue(order.contains("endSession"))
    }

    private class RecordingIntentionalLogoutMarker(
        private val order: MutableList<String>,
    ) : IntentionalLogoutMarker {
        override fun mark() {
            order += "mark"
        }

        override fun unmark() = Unit

        override fun consume(): Boolean = false
    }
}
