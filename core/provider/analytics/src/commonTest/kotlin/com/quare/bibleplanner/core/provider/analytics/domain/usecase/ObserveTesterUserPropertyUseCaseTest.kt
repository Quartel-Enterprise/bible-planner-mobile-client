package com.quare.bibleplanner.core.provider.analytics.domain.usecase

import com.quare.bibleplanner.core.provider.analytics.domain.service.AnalyticsService
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.impl.ObserveTesterUserPropertyUseCase
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.ObserveStringRemoteConfig
import com.quare.bibleplanner.core.user.domain.usecase.ObserveAuthenticatedUserId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveTesterUserPropertyUseCaseTest {
    private val userProperties = mutableMapOf<String, String?>()

    @Test
    fun `GIVEN a user in the allowlist WHEN observing THEN marks it as tester`() = runTest {
        // Given
        val useCase = createUseCase(allowlist = """["user-1","user-2"]""", userId = "user-1")

        // When
        backgroundScope.launch { useCase() }
        runCurrent()

        // Then
        assertEquals("true", userProperties[IS_TESTER])
    }

    @Test
    fun `GIVEN a user outside the allowlist WHEN observing THEN does not mark it as tester`() = runTest {
        // Given
        val useCase = createUseCase(allowlist = """["user-1","user-2"]""", userId = "user-3")

        // When
        backgroundScope.launch { useCase() }
        runCurrent()

        // Then
        assertEquals("false", userProperties[IS_TESTER])
    }

    @Test
    fun `GIVEN no authenticated user WHEN observing THEN does not mark it as tester`() = runTest {
        // Given
        val useCase = createUseCase(allowlist = """["user-1"]""", userId = null)

        // When
        backgroundScope.launch { useCase() }
        runCurrent()

        // Then
        assertEquals("false", userProperties[IS_TESTER])
    }

    private fun createUseCase(
        allowlist: String,
        userId: String?,
    ): ObserveTesterUserPropertyUseCase = ObserveTesterUserPropertyUseCase(
        observeAuthenticatedUserId = ObserveAuthenticatedUserId { MutableStateFlow(userId) },
        observeStringRemoteConfig = object : ObserveStringRemoteConfig {
            override fun invoke(
                key: String,
                default: String,
            ): Flow<String> = flowOf(allowlist)
        },
        analyticsService = FakeAnalyticsService(userProperties),
    )

    private class FakeAnalyticsService(
        private val userProperties: MutableMap<String, String?>,
    ) : AnalyticsService {
        override fun setUserProperty(
            name: String,
            value: String?,
        ) {
            userProperties[name] = value
        }

        override fun logEvent(
            name: String,
            params: Map<String, Any>,
        ) = Unit
    }

    private companion object {
        const val IS_TESTER = "is_tester"
    }
}
