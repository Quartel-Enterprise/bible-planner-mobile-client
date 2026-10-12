package com.quare.bibleplanner.core.chapterlistening.domain.usecase

import com.quare.bibleplanner.core.books.domain.usecase.GetChapterVerseContents
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterAudioModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSessionModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningVerseModel
import com.quare.bibleplanner.core.model.book.ChapterRef
import com.quare.bibleplanner.core.utils.suspendRunCatching
import kotlin.time.Duration

internal class GetListeningVersesUseCase(
    private val getChapterVerseContents: GetChapterVerseContents,
    private val getListeningWords: GetListeningWordsUseCase,
) {
    suspend operator fun invoke(
        audio: ChapterAudioModel,
        session: ListeningSessionModel,
    ): List<ListeningVerseModel> {
        val segment = session.segment
        val firstVerse = segment.startVerse ?: 0
        val lastVerse = segment.endVerse ?: Int.MAX_VALUE
        val contents = suspendRunCatching {
            getChapterVerseContents(
                ChapterRef(
                    bibleVersionId = session.bibleVersionId,
                    bookId = segment.chapter.bookId,
                    chapterNumber = segment.chapter.chapterNumber,
                ),
            )
        }.getOrDefault(emptyMap())
        val verses = audio.verses.filter { verse -> verse.number in firstVerse..lastVerse }
        return verses.mapIndexed { index, verse ->
            val next = verses.getOrNull(index + 1)
            val content = contents[verse.number]
            val text = content?.text.orEmpty()
            // Why: a whole chapter starts with its spoken title, and each verse with the heading read before it.
            val isTitleRead = index == 0 && (segment.startVerse ?: 1) <= 1
            ListeningVerseModel(
                number = verse.number,
                text = text,
                start = if (isTitleRead) Duration.ZERO else verse.headingStart ?: verse.start,
                end = when {
                    next != null -> next.headingStart ?: next.start
                    segment.endVerse == null -> audio.duration
                    else -> verse.end
                },
                words = getListeningWords(
                    titleWords = if (isTitleRead) audio.titleWords else emptyList(),
                    heading = content?.heading,
                    headingWords = verse.headingWords,
                    text = text,
                    words = verse.words,
                ),
            )
        }
    }
}
