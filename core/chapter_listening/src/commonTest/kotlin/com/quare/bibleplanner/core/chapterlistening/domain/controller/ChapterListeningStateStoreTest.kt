package com.quare.bibleplanner.core.chapterlistening.domain.controller

import com.quare.bibleplanner.core.chapterlistening.domain.mapper.createListeningSession
import com.quare.bibleplanner.core.chapterlistening.domain.mapper.toWholeChapterSegment
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterListeningEventModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterListeningSettingsModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterListeningStateModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningModeModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSleepTimerModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSleepTimerOption
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningStatusModel
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalCoroutinesApi::class)
internal class ChapterListeningStateStoreTest {
    private val store = ChapterListeningStateStore()
    private val session = createListeningSession(
        mode = ListeningModeModel.CHAPTER,
        day = null,
        segment = ChapterLocationModel(
            bookId = BookId.GEN,
            chapterNumber = 1,
        ).toWholeChapterSegment(),
        bibleVersionId = "ACF",
        shouldForceCanonOrder = false,
    )

    @Test
    fun `GIVEN a new store WHEN reading its state THEN nothing plays with the default settings and no sleep timer`() {
        // Given
        val expected = ChapterListeningStateModel(
            session = null,
            settings = ChapterListeningSettingsModel(
                speed = 1f,
                isAutoNextEnabled = true,
                isWordHighlightEnabled = true,
            ),
            sleepTimer = ListeningSleepTimerModel.Off,
        )

        // When
        val state = store.state.value

        // Then
        assertEquals(expected, state)
    }

    @Test
    fun `GIVEN a new state WHEN updating the store THEN its shortcuts read the new state`() {
        // Given
        val settings = ChapterListeningSettingsModel(
            speed = 1.5f,
            isAutoNextEnabled = false,
            isWordHighlightEnabled = false,
        )
        val sleepTimer = ListeningSleepTimerModel.Countdown(
            option = ListeningSleepTimerOption.FIFTEEN_MINUTES,
            remaining = 15.minutes,
        )

        // When
        store.update { state ->
            state.copy(
                session = session,
                settings = settings,
                sleepTimer = sleepTimer,
            )
        }

        // Then
        assertEquals(session, store.session)
        assertEquals(settings, store.settings)
        assertEquals(sleepTimer, store.sleepTimer)
    }

    @Test
    fun `GIVEN no session WHEN updating the session THEN there is still no session`() {
        // Given
        val status = ListeningStatusModel.PLAYING

        // When
        store.updateSession { it.copy(status = status) }

        // Then
        assertNull(store.session)
    }

    @Test
    fun `GIVEN a session WHEN updating it THEN applies the change to it`() {
        // Given
        store.update { it.copy(session = session) }

        // When
        store.updateSession { it.copy(status = ListeningStatusModel.PLAYING) }

        // Then
        assertEquals(session.copy(status = ListeningStatusModel.PLAYING), store.session)
    }

    @Test
    fun `GIVEN a collector WHEN emitting an event THEN the collector receives it`() =
        runTest(UnconfinedTestDispatcher()) {
            // Given
            val events = mutableListOf<ChapterListeningEventModel>()
            backgroundScope.launch { store.events.collect(events::add) }

            // When
            store.emit(ChapterListeningEventModel.SleepTimerEnded)

            // Then
            assertEquals(listOf<ChapterListeningEventModel>(ChapterListeningEventModel.SleepTimerEnded), events)
        }
}
