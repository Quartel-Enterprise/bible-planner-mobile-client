package com.quare.bibleplanner.core.chapterlistening.domain.usecase

import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterAudioWordModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningWordModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningWordTargetModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

internal class GetListeningWordsUseCaseTest {
    private val useCase = GetListeningWordsUseCase()
    private val text = "And God said, Let there be light."

    @Test
    fun `GIVEN spoken words WHEN mapping THEN marks each word without the punctuation after it`() {
        // Given
        val words = createWords(0, 4, 8, 14, 18, 24, 27)

        // When
        val listeningWords = mapVerse(words)

        // Then
        assertEquals(
            listOf("And", "God", "said", "Let", "there", "be", "light"),
            listeningWords.map { word -> text.substring(word.verseRange()) },
        )
    }

    @Test
    fun `GIVEN spoken words WHEN mapping THEN each word lasts until the next one starts`() {
        // Given
        val words = listOf(
            ChapterAudioWordModel(
                offset = 0,
                start = 1.seconds,
                end = 1.2.seconds,
            ),
            ChapterAudioWordModel(
                offset = 4,
                start = 1.5.seconds,
                end = 1.8.seconds,
            ),
        )

        // When
        val listeningWords = mapVerse(words)

        // Then
        assertEquals(
            listOf(
                ListeningWordModel(
                    target = ListeningWordTargetModel.Verse(0..2),
                    start = 1.seconds,
                    end = 1.5.seconds,
                ),
                ListeningWordModel(
                    target = ListeningWordTargetModel.Verse(4..6),
                    start = 1.5.seconds,
                    end = 1.8.seconds,
                ),
            ),
            listeningWords,
        )
    }

    @Test
    fun `GIVEN offsets counted from leading spaces WHEN mapping THEN marks the words of the trimmed text`() {
        // Given
        val words = createWords(1, 5, 9)

        // When
        val listeningWords = mapVerse(words)

        // Then
        assertEquals(listOf(0..2, 4..6, 8..11), listeningWords.map { word -> word.verseRange() })
    }

    @Test
    fun `GIVEN a word not starting on a letter WHEN mapping THEN leaves it out`() {
        // Given
        val words = createWords(0, 3, 8)

        // When
        val listeningWords = mapVerse(words)

        // Then
        assertEquals(listOf(0..2, 8..11), listeningWords.map { word -> word.verseRange() })
    }

    @Test
    fun `GIVEN a word that lands mid-word WHEN mapping THEN leaves it out`() {
        // Given
        val words = createWords(0, 5, 8)

        // When
        val listeningWords = mapVerse(words)

        // Then
        assertEquals(listOf(0..2, 8..11), listeningWords.map { word -> word.verseRange() })
    }

    @Test
    fun `GIVEN a word left out WHEN mapping THEN the word before it ends on its own end`() {
        // Given
        val words = createWords(0, 3, 8)

        // When
        val listeningWords = mapVerse(words)

        // Then
        assertEquals(listOf(0.5.seconds, 2.5.seconds), listeningWords.map(ListeningWordModel::end))
    }

    @Test
    fun `GIVEN heading words without a local heading WHEN mapping THEN the title ends on its own end`() {
        // Given
        val titleWords = createWords(0, 8)

        // When
        val listeningWords = useCase(
            titleWords = titleWords,
            heading = null,
            headingWords = createWords(0, 4),
            text = text,
            words = emptyList(),
        )

        // Then
        assertEquals(listOf(1.seconds, 1.5.seconds), listeningWords.map(ListeningWordModel::end))
    }

    @Test
    fun `GIVEN apostrophes WHEN mapping THEN keeps a possessive and drops a closing quote`() {
        // Given
        val quoted = "Abraham's son said 'Go' now"
        val words = createWords(0, 10, 14, 20, 24)

        // When
        val listeningWords = useCase(
            titleWords = emptyList(),
            heading = null,
            headingWords = emptyList(),
            text = quoted,
            words = words,
        )

        // Then
        assertEquals(
            listOf("Abraham's", "son", "said", "Go", "now"),
            listeningWords.map { word -> quoted.substring(word.verseRange()) },
        )
    }

    @Test
    fun `GIVEN a spoken title and heading WHEN mapping THEN marks them before the verse in reading order`() {
        // Given
        val titleWords = createWords(0, 8)
        val headingWords = createWords(0, 4)
        val words = createWords(0)

        // When
        val listeningWords = useCase(
            titleWords = titleWords,
            heading = "The Fall",
            headingWords = headingWords,
            text = text,
            words = words,
        )

        // Then
        assertEquals(
            listOf(
                ListeningWordTargetModel.ChapterBook,
                ListeningWordTargetModel.ChapterNumber,
                ListeningWordTargetModel.Heading(0..2),
                ListeningWordTargetModel.Heading(4..7),
                ListeningWordTargetModel.Verse(0..2),
            ),
            listeningWords.map(ListeningWordModel::target),
        )
    }

    @Test
    fun `GIVEN a book name of several words WHEN mapping the title THEN only the last word is the chapter number`() {
        // Given
        val titleWords = createWords(0, 2, 9)

        // When
        val listeningWords = useCase(
            titleWords = titleWords,
            heading = null,
            headingWords = emptyList(),
            text = text,
            words = emptyList(),
        )

        // Then
        assertEquals(
            listOf(
                ListeningWordTargetModel.ChapterBook,
                ListeningWordTargetModel.ChapterBook,
                ListeningWordTargetModel.ChapterNumber,
            ),
            listeningWords.map(ListeningWordModel::target),
        )
    }

    @Test
    fun `GIVEN no words WHEN mapping THEN has nothing to highlight`() {
        // Given
        val words = emptyList<ChapterAudioWordModel>()

        // When
        val listeningWords = mapVerse(words)

        // Then
        assertEquals(emptyList(), listeningWords)
    }

    private fun mapVerse(words: List<ChapterAudioWordModel>): List<ListeningWordModel> = useCase(
        titleWords = emptyList(),
        heading = null,
        headingWords = emptyList(),
        text = text,
        words = words,
    )

    private fun createWords(vararg offsets: Int): List<ChapterAudioWordModel> = offsets.mapIndexed { index, offset ->
        ChapterAudioWordModel(
            offset = offset,
            start = index.seconds,
            end = index.seconds + 0.5.seconds,
        )
    }

    private fun ListeningWordModel.verseRange(): IntRange = (target as ListeningWordTargetModel.Verse).range
}
