package com.quare.bibleplanner.feature.bibleversion.presentation

import com.quare.bibleplanner.core.date.HasCooldownElapsedUseCase
import com.quare.bibleplanner.feature.bibleversion.domain.BibleUpdatePromptPreferences
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.GetPendingBibleUpdatesUseCase
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.ShouldShowBibleUpdatePromptUseCase
import com.quare.bibleplanner.feature.bibleversion.fake.FakeBibleRepository
import com.quare.bibleplanner.feature.bibleversion.fake.bibleModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class PendingBibleUpdatesPromptViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: PendingBibleUpdatesPromptViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `prompts when a version has a pending update`() = runTest(testDispatcher) {
        // Given
        prepareScenario(hasPendingUpdate = true)

        // When
        val shouldPrompt = viewModel.shouldPrompt.value

        // Then
        assertTrue(shouldPrompt)
    }

    @Test
    fun `does not prompt when every version is up to date`() = runTest(testDispatcher) {
        // Given
        prepareScenario(hasPendingUpdate = false)

        // When
        val shouldPrompt = viewModel.shouldPrompt.value

        // Then
        assertFalse(shouldPrompt)
    }

    @Test
    fun `stops prompting once the prompt is consumed`() = runTest(testDispatcher) {
        // Given
        prepareScenario(hasPendingUpdate = true)

        // When
        viewModel.onPromptConsumed()

        // Then
        assertFalse(viewModel.shouldPrompt.value)
    }

    private fun prepareScenario(hasPendingUpdate: Boolean) {
        viewModel = PendingBibleUpdatesPromptViewModel(
            ShouldShowBibleUpdatePromptUseCase(
                getPendingBibleUpdates = GetPendingBibleUpdatesUseCase(
                    FakeBibleRepository(
                        listOf(
                            bibleModel(
                                id = "ACF",
                                hasPendingUpdate = hasPendingUpdate,
                            ),
                        ),
                    ),
                ),
                bibleUpdatePromptPreferences = NeverDismissedPromptPreferences(),
                hasCooldownElapsed = HasCooldownElapsedUseCase { NOW },
            ),
        )
    }

    private companion object {
        const val NOW = 1_700_000_000_000L
    }
}

private class NeverDismissedPromptPreferences : BibleUpdatePromptPreferences {
    override suspend fun getLastDismissedAt(): Long? = null

    override suspend fun setLastDismissedAt(timestamp: Long) = error("Unexpected call")
}
