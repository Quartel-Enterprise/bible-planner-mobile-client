package com.quare.bibleplanner.core.remoteconfig.domain.usecase.web.impl

import com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.impl.GetStringRemoteConfigUseCase
import com.quare.bibleplanner.core.remoteconfig.fake.FakeRemoteConfigService
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class GetWebAppUrlUseCaseTest {
    private lateinit var useCase: GetWebAppUrlUseCase

    @Test
    fun `GIVEN a remote web app url WHEN reading it THEN returns the remote url`() = runTest {
        // Given
        prepareScenario(mapOf(URL_KEY to "https://beta.bibleplanner.app/"))

        // When
        val url = useCase()

        // Then
        assertEquals("https://beta.bibleplanner.app/", url)
    }

    @Test
    fun `GIVEN a blank remote web app url WHEN reading it THEN falls back to the production url`() = runTest {
        // Given
        prepareScenario(mapOf(URL_KEY to " "))

        // When
        val url = useCase()

        // Then
        assertEquals("https://web.bibleplanner.app/", url)
    }

    @Test
    fun `GIVEN no remote web app url WHEN reading it THEN falls back to the production url`() = runTest {
        // Given
        prepareScenario(emptyMap())

        // When
        val url = useCase()

        // Then
        assertEquals("https://web.bibleplanner.app/", url)
    }

    private fun prepareScenario(values: Map<String, Any>) {
        useCase = GetWebAppUrlUseCase(GetStringRemoteConfigUseCase(FakeRemoteConfigService(values)))
    }

    private companion object {
        const val URL_KEY = "profile_web_app_url"
    }
}
