package com.quare.bibleplanner.core.remoteconfig.domain.usecase.web.impl

import com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.impl.ObserveBooleanRemoteConfigUseCase
import com.quare.bibleplanner.core.remoteconfig.fake.FakeRemoteConfigService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class ObserveProfileWebAppEnabledUseCaseTest {
    private lateinit var useCase: ObserveProfileWebAppEnabledUseCase

    @Test
    fun `GIVEN the web app flag on WHEN observing it THEN emits enabled`() = runTest {
        // Given
        prepareScenario(mapOf(ENABLED_KEY to true))

        // When
        val isEnabled = useCase().first()

        // Then
        assertTrue(isEnabled)
    }

    @Test
    fun `GIVEN no web app flag WHEN observing it THEN emits disabled`() = runTest {
        // Given
        prepareScenario(emptyMap())

        // When
        val isEnabled = useCase().first()

        // Then
        assertFalse(isEnabled)
    }

    private fun prepareScenario(values: Map<String, Any>) {
        useCase = ObserveProfileWebAppEnabledUseCase(ObserveBooleanRemoteConfigUseCase(FakeRemoteConfigService(values)))
    }

    private companion object {
        const val ENABLED_KEY = "profile_web_app_enabled"
    }
}
