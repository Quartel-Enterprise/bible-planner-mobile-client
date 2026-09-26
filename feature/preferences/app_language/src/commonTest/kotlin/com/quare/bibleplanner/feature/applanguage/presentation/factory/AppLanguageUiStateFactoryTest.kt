package com.quare.bibleplanner.feature.applanguage.presentation.factory

import com.quare.bibleplanner.core.provider.language.domain.provider.LanguageProvider
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.applanguage.domain.usecase.impl.GetLanguageSyncEnabledFlowUseCase
import com.quare.bibleplanner.feature.applanguage.presentation.FakeAppLanguageRepository
import com.quare.bibleplanner.feature.applanguage.presentation.model.AppLanguageUiState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

internal class AppLanguageUiStateFactoryTest {
    private lateinit var factory: AppLanguageUiStateFactory

    @Test
    fun `GIVEN the app language WHEN creating the initial state THEN selects it before the stored values load`() {
        // Given
        prepareScenario(appLanguage = Language.SPANISH)

        // When
        val state = factory.createInitial()

        // Then
        assertEquals(
            AppLanguageUiState(
                selectedLanguage = Language.SPANISH,
                languages = Language.entries,
                isSyncEnabled = false,
                isLoggedIn = false,
            ),
            state,
        )
    }

    @Test
    fun `GIVEN a logged in user with sync on WHEN creating the state THEN exposes the stored language and sync`() =
        runTest {
            // Given
            prepareScenario(
                storedLanguage = Language.PORTUGUESE_BRAZIL,
                isSyncEnabled = true,
                userId = "user-1",
            )

            // When
            val state = factory.create().first()

            // Then
            assertEquals(
                AppLanguageUiState(
                    selectedLanguage = Language.PORTUGUESE_BRAZIL,
                    languages = Language.entries,
                    isSyncEnabled = true,
                    isLoggedIn = true,
                ),
                state,
            )
        }

    @Test
    fun `GIVEN a logged out user WHEN creating the state THEN reports the user as logged out`() = runTest {
        // Given
        prepareScenario(userId = null)

        // When
        val state = factory.create().first()

        // Then
        assertFalse(state.isLoggedIn)
    }

    private fun prepareScenario(
        appLanguage: Language = Language.ENGLISH,
        storedLanguage: Language = Language.ENGLISH,
        isSyncEnabled: Boolean = false,
        userId: String? = "user-1",
    ) {
        val repository = FakeAppLanguageRepository(
            initialLanguage = storedLanguage,
            initialSyncEnabled = isSyncEnabled,
        )
        factory = AppLanguageUiStateFactory(
            getAppLanguageFlow = repository::getLanguageFlow,
            getLanguageSyncEnabledFlow = GetLanguageSyncEnabledFlowUseCase(repository),
            observeAuthenticatedUserId = { flowOf(userId) },
            languageProvider = FixedAppLanguageProvider(appLanguage),
        )
    }
}

private class FixedAppLanguageProvider(
    private val appLanguage: Language,
) : LanguageProvider {
    override fun getDeviceLanguage(): Language = error("unused")

    override fun getAppLanguage(): Language = appLanguage
}
