package com.quare.bibleplanner.feature.bibleversion.presentation

import com.quare.bibleplanner.core.books.testing.FakeBibleRepository
import com.quare.bibleplanner.core.date.HasCooldownElapsedUseCase
import com.quare.bibleplanner.feature.bibleversion.domain.BibleUpdatePromptPreferences
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.GetPendingBibleUpdatesUseCase
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.ShouldShowBibleUpdatePromptUseCase
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
    fun `GIVEN a version with a pending update WHEN reading the prompt state THEN prompts`() = runTest(testDispatcher) {
        // Given
        prepareScenario(hasPendingUpdate = true)

        // When
        val shouldPrompt = viewModel.shouldPrompt.value

        // Then
        assertTrue(shouldPrompt)
    }

    @Test
    fun `GIVEN every version up to date WHEN reading the prompt state THEN does not prompt`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(hasPendingUpdate = false)

            // When
            val shouldPrompt = viewModel.shouldPrompt.value

            // Then
            assertFalse(shouldPrompt)
        }

    @Test
    fun `GIVEN a pending prompt WHEN consuming it THEN stops prompting`() = runTest(testDispatcher) {
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
                        bibles = listOf(
                            bibleModel(
                                id = "ACF",
                                hasPendingUpdate = hasPendingUpdate,
                            ),
                        ),
                        selectedVersionId = "ACF",
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
