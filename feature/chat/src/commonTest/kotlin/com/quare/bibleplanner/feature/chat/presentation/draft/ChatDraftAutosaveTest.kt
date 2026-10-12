package com.quare.bibleplanner.feature.chat.presentation.draft

import com.quare.bibleplanner.core.utils.coroutines.ApplicationScope
import com.quare.bibleplanner.feature.chat.domain.model.PendingDraftModel
import com.quare.bibleplanner.feature.chat.domain.repository.ChatRepository
import com.quare.bibleplanner.feature.chat.domain.repository.FakeChatRepository
import com.quare.bibleplanner.feature.chat.domain.usecase.SaveChatDraftUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
internal class ChatDraftAutosaveTest {
    private val debounceDelay: Duration = 2.seconds
    private val testDispatcher = StandardTestDispatcher()
    private val firstDraft = PendingDraftModel(
        threadKey = "day:CHRONOLOGICAL:1:4",
        content = "Por que Caim",
    )
    private val secondDraft = PendingDraftModel(
        threadKey = "conversation-1",
        content = "Por que Caim matou Abel?",
    )
    private lateinit var repository: RecordingChatRepository
    private lateinit var screenScope: CoroutineScope
    private lateinit var autosave: ChatDraftAutosave

    @BeforeTest
    fun setUp() {
        repository = RecordingChatRepository()
        screenScope = CoroutineScope(testDispatcher)
        autosave = ChatDraftAutosave(
            saveDraft = SaveChatDraftUseCase(repository),
            applicationScope = ApplicationScope(CoroutineScope(testDispatcher)),
        )
    }

    @Test
    fun `GIVEN a scheduled draft WHEN the debounce has not elapsed THEN nothing is saved`() = runTest(testDispatcher) {
        // Given
        autosave.schedule(
            scope = screenScope,
            draft = firstDraft,
        )

        // When
        advanceTimeBy(debounceDelay - 1.milliseconds)

        // Then
        assertTrue(repository.savedDrafts.isEmpty())
    }

    @Test
    fun `GIVEN a scheduled draft WHEN the debounce elapses THEN the draft is saved`() = runTest(testDispatcher) {
        // Given
        autosave.schedule(
            scope = screenScope,
            draft = firstDraft,
        )

        // When
        advanceTimeBy(debounceDelay)
        runCurrent()

        // Then
        assertEquals(listOf(firstDraft), repository.savedDrafts)
    }

    @Test
    fun `GIVEN a draft waiting for its debounce WHEN a newer one is scheduled THEN only the newer is saved`() =
        runTest(testDispatcher) {
            // Given
            autosave.schedule(
                scope = screenScope,
                draft = firstDraft,
            )
            advanceTimeBy(debounceDelay / 2)

            // When
            autosave.schedule(
                scope = screenScope,
                draft = secondDraft,
            )
            advanceUntilIdle()

            // Then
            assertEquals(listOf(secondDraft), repository.savedDrafts)
        }

    @Test
    fun `GIVEN a draft waiting for its debounce WHEN clearing THEN only an empty draft is saved for the thread`() =
        runTest(testDispatcher) {
            // Given
            autosave.schedule(
                scope = screenScope,
                draft = firstDraft,
            )

            // When
            autosave.clear(
                scope = screenScope,
                threadKey = secondDraft.threadKey,
            )
            advanceUntilIdle()

            // Then
            val cleared = PendingDraftModel(
                threadKey = secondDraft.threadKey,
                content = "",
            )
            assertEquals(listOf(cleared), repository.savedDrafts)
        }

    @Test
    fun `GIVEN a cleared draft WHEN flushing THEN nothing else is saved`() = runTest(testDispatcher) {
        // Given
        autosave.schedule(
            scope = screenScope,
            draft = firstDraft,
        )
        autosave.clear(
            scope = screenScope,
            threadKey = firstDraft.threadKey,
        )

        // When
        autosave.flush()
        advanceUntilIdle()

        // Then
        val cleared = PendingDraftModel(
            threadKey = firstDraft.threadKey,
            content = "",
        )
        assertEquals(listOf(cleared), repository.savedDrafts)
    }

    @Test
    fun `GIVEN a draft waiting for its debounce WHEN flushing and the screen scope ends THEN it is saved once`() =
        runTest(testDispatcher) {
            // Given
            autosave.schedule(
                scope = screenScope,
                draft = firstDraft,
            )
            advanceTimeBy(debounceDelay / 2)

            // When
            autosave.flush()
            screenScope.cancel()
            advanceUntilIdle()

            // Then
            assertEquals(listOf(firstDraft), repository.savedDrafts)
        }

    @Test
    fun `GIVEN a draft already saved WHEN flushing THEN it is not saved again`() = runTest(testDispatcher) {
        // Given
        autosave.schedule(
            scope = screenScope,
            draft = firstDraft,
        )
        advanceUntilIdle()

        // When
        autosave.flush()
        advanceUntilIdle()

        // Then
        assertEquals(listOf(firstDraft), repository.savedDrafts)
    }

    @Test
    fun `GIVEN no pending draft WHEN flushing THEN nothing is saved`() = runTest(testDispatcher) {
        // When
        autosave.flush()
        advanceUntilIdle()

        // Then
        assertTrue(repository.savedDrafts.isEmpty())
    }

    @Test
    fun `GIVEN an input equal to the draft WHEN finding the input to apply THEN returns null`() {
        // Given
        val draft = "Por que Caim"

        // When
        val input = autosave.findInputToApply(
            draft = draft,
            input = draft,
        )

        // Then
        assertNull(input)
    }

    @Test
    fun `GIVEN an input that matched an earlier draft WHEN a new draft arrives THEN returns the new draft`() {
        // Given
        autosave.findInputToApply(
            draft = "Por que Caim",
            input = "Por que Caim",
        )

        // When
        val input = autosave.findInputToApply(
            draft = "",
            input = "Por que Caim",
        )

        // Then
        assertEquals("", input)
    }

    @Test
    fun `GIVEN an empty input WHEN finding the input to apply THEN returns the draft`() {
        // Given
        val draft = "Por que Caim"

        // When
        val input = autosave.findInputToApply(
            draft = draft,
            input = "",
        )

        // Then
        assertEquals(draft, input)
    }

    @Test
    fun `GIVEN an input equal to the last applied draft WHEN a new draft arrives THEN returns the new draft`() {
        // Given
        autosave.findInputToApply(
            draft = "Por que Caim",
            input = "",
        )

        // When
        val input = autosave.findInputToApply(
            draft = "Por que Caim matou Abel?",
            input = "Por que Caim",
        )

        // Then
        assertEquals("Por que Caim matou Abel?", input)
    }

    @Test
    fun `GIVEN typed text different from the last applied draft WHEN a new draft arrives THEN returns null`() {
        // Given
        autosave.findInputToApply(
            draft = "Por que Caim",
            input = "",
        )

        // When
        val input = autosave.findInputToApply(
            draft = "",
            input = "Onde ficava o Éden?",
        )

        // Then
        assertNull(input)
    }
}

private class RecordingChatRepository : ChatRepository by FakeChatRepository() {
    val savedDrafts: MutableList<PendingDraftModel> = mutableListOf()

    override suspend fun saveDraft(draft: PendingDraftModel) {
        savedDrafts += draft
    }
}
