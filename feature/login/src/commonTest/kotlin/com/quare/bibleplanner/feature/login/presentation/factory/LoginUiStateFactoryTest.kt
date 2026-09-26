package com.quare.bibleplanner.feature.login.presentation.factory

import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.feature.login.domain.model.LoginProvider
import com.quare.bibleplanner.feature.login.presentation.model.LoginUiState
import kotlin.test.Test
import kotlin.test.assertEquals

internal class LoginUiStateFactoryTest {
    private lateinit var factory: LoginUiStateFactory

    @Test
    fun `GIVEN an apple platform WHEN creating the state THEN lists apple before google`() {
        // Given
        prepareScenario(platform = Platform.Ios)

        // When
        val state = factory.create()

        // Then
        assertEquals(
            LoginUiState(
                enabledProviders = listOf(LoginProvider.APPLE, LoginProvider.GOOGLE),
                loadingProvider = null,
                error = null,
                showGoogleSignInUnavailableDialog = false,
            ),
            state,
        )
    }

    @Test
    fun `GIVEN a non apple platform WHEN creating the state THEN lists google before apple`() {
        // Given
        prepareScenario(platform = Platform.Android)

        // When
        val state = factory.create()

        // Then
        assertEquals(listOf(LoginProvider.GOOGLE, LoginProvider.APPLE), state.enabledProviders)
    }

    private fun prepareScenario(platform: Platform) {
        factory = LoginUiStateFactory(platform)
    }
}
