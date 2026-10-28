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
        val headingTargets = findRanges(
            text = heading.orEmpty(),
            words = headingWords,
        ).map { (word, range) -> word to range?.let(ListeningWordTargetModel::Heading) }
        val verseTargets = findRanges(
            text = text,
            words = words,
        ).map { (word, range) -> word to range?.let(ListeningWordTargetModel::Verse) }
        val spoken = title + headingTargets + verseTargets
        return spoken.mapIndexedNotNull { index, (word, target) ->
            if (target == null) return@mapIndexedNotNull null
            val next = spoken.getOrNull(index + 1)
            ListeningWordModel(
                target = target,
                start = word.start,
                // Why: a word stays lit through the pause after it, but not over a word left out after it.
                end = next?.takeIf { (_, nextTarget) -> nextTarget != null }?.first?.start ?: word.end,
            )
        }
    }

    // Why: a word that doesn't land on the start of a word gets no range, so it is never highlighted.
    private fun findRanges(
        text: String,
        words: List<ChapterAudioWordModel>,
    ): List<Pair<ChapterAudioWordModel, IntRange?>> {
        val firstWord = words.firstOrNull() ?: return emptyList()
        val firstLetter = text.indexOfFirst(Char::isLetterOrDigit)
        if (firstLetter < 0) return words.map { word -> word to null }
        // Why: the offsets count from the server's untrimmed text, and the first spoken word starts the trimmed one.
        val shift = firstWord.offset - firstLetter
        val starts = words.map { word -> word.offset - shift }
        return words.mapIndexed { index, word ->
            val start = starts[index]
            val isAtWordStart = isWordStart(
                text = text,
                index = start,
            )
            val range = if (isAtWordStart) {
                val limit = starts.getOrNull(index + 1)?.coerceIn(start + 1, text.length) ?: text.length
                start until findWordEnd(
                    text = text,
                    start = start,
                    limit = limit,
                )
            } else {
                null
            }
            word to range
        }
    }

    // Why: a text that changed after the audio was made would otherwise highlight the wrong letters or half a word.
    private fun isWordStart(
        text: String,
        index: Int,
    ): Boolean = index in text.indices &&
        text[index].isLetterOrDigit() &&
        (index == 0 || !text[index - 1].isLetterOrDigit())

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
