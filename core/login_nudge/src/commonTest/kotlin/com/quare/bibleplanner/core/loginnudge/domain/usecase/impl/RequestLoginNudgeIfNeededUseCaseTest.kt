package com.quare.bibleplanner.core.loginnudge.domain.usecase.impl

import com.quare.bibleplanner.core.model.NavigationEventBus
import com.quare.bibleplanner.core.model.route.LoginSyncNudgeNavRoute
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class RequestLoginNudgeIfNeededUseCaseTest {
    @Test
    fun `sends the nudge route when the nudge should be shown`() = runTest {
        val navigationEventBus = NavigationEventBus()
        val useCase = RequestLoginNudgeIfNeededUseCase(
            shouldShowLoginNudge = { true },
            navigationEventBus = navigationEventBus,
        )

        useCase()

        assertEquals(LoginSyncNudgeNavRoute, navigationEventBus.events.first())
    }

    @Test
    fun `sends nothing when the nudge should not be shown`() = runTest {
        val navigationEventBus = NavigationEventBus()
        val useCase = RequestLoginNudgeIfNeededUseCase(
            shouldShowLoginNudge = { false },
            navigationEventBus = navigationEventBus,
        )

        useCase()

        assertNull(navigationEventBus.events.replayCache.firstOrNull())
    }
}
