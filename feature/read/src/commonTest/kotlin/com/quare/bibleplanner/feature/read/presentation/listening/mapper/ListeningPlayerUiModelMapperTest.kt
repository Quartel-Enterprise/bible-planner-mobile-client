package com.quare.bibleplanner.feature.read.presentation.listening.mapper

import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningDayModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSegmentModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningStatusModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningWordTargetModel
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.GetListeningTimeUseCase
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import com.quare.bibleplanner.core.model.plan.PlanDayLocationModel
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.feature.read.fixture.listeningSession
import com.quare.bibleplanner.feature.read.presentation.listening.model.ListeningDayProgressUiModel
import com.quare.bibleplanner.feature.read.presentation.listening.model.ListeningPlayerUiModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

internal class ListeningPlayerUiModelMapperTest {
    private val mapper = ListeningPlayerUiModelMapper(GetListeningTimeUseCase())
    private val genesisOne = ChapterLocationModel(
        bookId = BookId.GEN,
        chapterNumber = 1,
    )
    private val genesisTwo = ChapterLocationModel(
        bookId = BookId.GEN,
        chapterNumber = 2,
    )

    @Test
    fun `GIVEN a session halfway through its first verse WHEN mapping THEN shows the verse progress and time`() {
        // Given
        val session = listeningSession(chapter = genesisOne)

        // When
        val player = mapper.map(
            session = session,
            speed = 1f,
        )

        // Then
        assertEquals(
            ListeningPlayerUiModel(
                chapter = genesisOne,
                verseNumber = 1,
                verseText = "one two three four five",
                spokenWord = null,
                hasWordTimings = true,
                verseIndex = 0,
                verseCount = 2,
                status = ListeningStatusModel.PLAYING,
                progress = 0.25f,
                elapsed = 1.seconds,
                remaining = 3.seconds,
                total = 4.seconds,
                bibleVersionId = "web",
                versionAbbreviation = "WEB",
                dayProgress = null,
                lockedChapter = null,
            ),
            player,
        )
    }

    @Test
    fun `GIVEN the reading of today WHEN mapping THEN shows where the chapter is in the day`() {
        // Given
        val day = ListeningDayModel(
            location = PlanDayLocationModel(
                weekNumber = 1,
                dayNumber = 1,
                readingPlanType = ReadingPlanType.BOOKS,
            ),
            segments = listOf(genesisOne, genesisTwo).map { chapter ->
                ListeningSegmentModel(
                    chapter = chapter,
                    startVerse = null,
                    endVerse = null,
                )
            },
        )
        val session = listeningSession(
            chapter = genesisTwo,
            day = day,
            lockedChapter = genesisOne,
        )

        // When
        val player = mapper.map(
            session = session,
            speed = 1f,
        )

        // Then
        assertEquals(
            ListeningDayProgressUiModel(
                chapters = listOf(genesisOne, genesisTwo),
                currentIndex = 1,
            ),
            player.dayProgress,
        )
        assertEquals(genesisOne, player.lockedChapter)
    }

    @Test
    fun `GIVEN a session still loading its verses WHEN mapping THEN shows no progress`() {
        // Given
        val session = listeningSession(
            chapter = genesisOne,
            status = ListeningStatusModel.PREPARING,
        ).copy(verses = emptyList())

        // When
        val player = mapper.map(
            session = session,
            speed = 1f,
        )

        // Then
        assertEquals(0f, player.progress)
        assertEquals(null, player.verseNumber)
    }

    @Test
    fun `GIVEN a word being read WHEN mapping THEN highlights its place in the verse text`() {
        // Given
        val session = listeningSession(
            chapter = genesisOne,
            wordIndex = 2,
        )

        // When
        val player = mapper.map(
            session = session,
            speed = 1f,
        )

        // Then
        val word = player.spokenWord as ListeningWordTargetModel.Verse
        assertEquals("two", player.verseText?.substring(word.range))
    }

    @Test
    fun `GIVEN a verse without word timings WHEN mapping THEN says it has none`() {
        // Given
        val session = listeningSession(
            chapter = genesisOne,
            verseIndex = 1,
        )

        // When
        val player = mapper.map(
            session = session,
            speed = 1f,
        )

        // Then
        assertEquals(false, player.hasWordTimings)
    }
}
