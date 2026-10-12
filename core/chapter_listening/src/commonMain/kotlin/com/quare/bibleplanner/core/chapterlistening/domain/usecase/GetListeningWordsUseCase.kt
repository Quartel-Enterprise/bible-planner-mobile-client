package com.quare.bibleplanner.core.chapterlistening.domain.usecase

import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterAudioWordModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningWordModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningWordTargetModel

class GetListeningWordsUseCase {
    operator fun invoke(
        titleWords: List<ChapterAudioWordModel>,
        heading: String?,
        headingWords: List<ChapterAudioWordModel>,
        text: String,
        words: List<ChapterAudioWordModel>,
    ): List<ListeningWordModel> {
        val title = titleWords.mapIndexed { index, word ->
            // Why: the spoken title is the book name followed by the chapter number, which the reader shows apart.
            val isChapterNumber = titleWords.size > 1 && index == titleWords.lastIndex
            word to
                if (isChapterNumber) ListeningWordTargetModel.ChapterNumber else ListeningWordTargetModel.ChapterBook
        }
        val headingRanges = heading
            ?.let { headingText ->
                findRanges(
                    text = headingText,
                    words = headingWords,
                )
            }.orEmpty()
            .map { (word, range) -> word to ListeningWordTargetModel.Heading(range) }
        val verseRanges = findRanges(
            text = text,
            words = words,
        ).map { (word, range) -> word to ListeningWordTargetModel.Verse(range) }
        val spoken = title + headingRanges + verseRanges
        return spoken.mapIndexed { index, (word, target) ->
            ListeningWordModel(
                target = target,
                start = word.start,
                // Why: a word stays highlighted through the pause after it, so the highlight doesn't flicker.
                end = spoken.getOrNull(index + 1)?.first?.start ?: word.end,
            )
        }
    }

    private fun findRanges(
        text: String,
        words: List<ChapterAudioWordModel>,
    ): List<Pair<ChapterAudioWordModel, IntRange>> {
        val firstWord = words.firstOrNull() ?: return emptyList()
        val firstLetter = text.indexOfFirst(Char::isLetterOrDigit)
        if (firstLetter < 0) return emptyList()
        // Why: the offsets count from the server's untrimmed text, and the first spoken word starts the trimmed one.
        val shift = firstWord.offset - firstLetter
        val starts = words.map { word -> word.offset - shift }
        return words.mapIndexedNotNull { index, word ->
            val start = starts[index]
            // Why: a text that changed after the audio was made would highlight the wrong letters.
            if (start !in text.indices || !text[start].isLetterOrDigit()) return@mapIndexedNotNull null
            val limit = starts.getOrNull(index + 1)?.coerceIn(start + 1, text.length) ?: text.length
            val end = findWordEnd(
                text = text,
                start = start,
                limit = limit,
            )
            word to (start until end)
        }
    }

    private fun findWordEnd(
        text: String,
        start: Int,
        limit: Int,
    ): Int {
        var end = start
        while (end < limit && (text[end].isLetterOrDigit() || text[end] in WORD_JOINERS)) end++
        // Why: a joiner at the end is a closing quote or a dash, not part of the word.
        while (text[end - 1] in WORD_JOINERS) end--
        return end
    }

    private companion object {
        const val WORD_JOINERS = "'’-"
    }
}
