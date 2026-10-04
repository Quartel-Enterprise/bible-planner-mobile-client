package com.quare.bibleplanner.core.verseannotations.domain.usecase

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterRef
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.GetBooleanRemoteConfig
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.GetIntRemoteConfig
import com.quare.bibleplanner.core.verseannotations.domain.model.VerseNote
import com.quare.bibleplanner.core.verseannotations.domain.usecase.impl.GetMaxFreeVerseNotesAmountUseCase
import com.quare.bibleplanner.core.verseannotations.domain.usecase.impl.ShouldBlockAddVerseNoteUseCase
import com.quare.bibleplanner.core.verseannotations.fake.FakeVerseNoteRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class ShouldBlockAddVerseNoteUseCaseTest {
    private lateinit var useCase: ShouldBlockAddVerseNoteUseCase
    private lateinit var requestedBooleanKeys: MutableList<String>

    @Test
    fun `GIVEN the limit is enabled and a free user is at it WHEN checking THEN blocks new notes`() = runTest {
        // Given
        prepareScenario(
            isLimitEnabled = true,
            isFreeUser = true,
            notesCount = 3,
        )

        // When
        val shouldBlock = useCase()

        // Then
        assertTrue(shouldBlock)
        assertEquals(
            expected = listOf("verse_notes_limit_enabled"),
            actual = requestedBooleanKeys,
        )
    }

    @Test
    fun `GIVEN the limit is enabled and a free user is under it WHEN checking THEN allows new notes`() = runTest {
        // Given
        prepareScenario(
            isLimitEnabled = true,
            isFreeUser = true,
            notesCount = 2,
        )

        // When
        val shouldBlock = useCase()

        // Then
        assertFalse(shouldBlock)
    }

    @Test
    fun `GIVEN the limit is enabled and a pro user is over it WHEN checking THEN allows new notes`() = runTest {
        // Given
        prepareScenario(
            isLimitEnabled = true,
            isFreeUser = false,
            notesCount = 10,
        )

        // When
        val shouldBlock = useCase()

        // Then
        assertFalse(shouldBlock)
    }

    @Test
    fun `GIVEN the limit is disabled and a free user is over it WHEN checking THEN allows new notes`() = runTest {
        // Given
        prepareScenario(
            isLimitEnabled = false,
            isFreeUser = true,
            notesCount = 10,
        )

        // When
        val shouldBlock = useCase()

        // Then
        assertFalse(shouldBlock)
    }

    private fun note(index: Int): VerseNote = VerseNote(
        id = "note-$index",
        chapter = ChapterRef(
            bibleVersionId = "ACF",
            bookId = BookId.GEN,
            chapterNumber = 1,
        ),
        verseNumbers = listOf(index + 1),
        text = "Note $index",
        createdAtEpochMillis = 1L,
        updatedAtEpochMillis = 1L,
    )

    private fun prepareScenario(
        isLimitEnabled: Boolean,
        isFreeUser: Boolean,
        notesCount: Int,
    ) {
        requestedBooleanKeys = mutableListOf()
        useCase = ShouldBlockAddVerseNoteUseCase(
            verseNoteRepository = FakeVerseNoteRepository(initialNotes = List(notesCount, ::note)),
            getMaxFreeVerseNotesAmount = GetMaxFreeVerseNotesAmountUseCase(FixedIntRemoteConfig(value = 3)),
            getBooleanRemoteConfig = RecordingBooleanRemoteConfig(
                value = isLimitEnabled,
                requestedKeys = requestedBooleanKeys,
            ),
            isFreeUser = { isFreeUser },
        )
    }
}

private class FixedIntRemoteConfig(
    private val value: Int,
) : GetIntRemoteConfig {
    override suspend fun invoke(
        key: String,
        default: Int,
    ): Int = value
}

private class RecordingBooleanRemoteConfig(
    private val value: Boolean,
    private val requestedKeys: MutableList<String>,
) : GetBooleanRemoteConfig {
    override suspend fun invoke(
        key: String,
        default: Boolean,
    ): Boolean {
        requestedKeys += key
        return value
    }
}
