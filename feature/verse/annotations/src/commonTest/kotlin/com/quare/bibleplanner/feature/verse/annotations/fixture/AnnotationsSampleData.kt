package com.quare.bibleplanner.feature.verse.annotations.fixture

import com.quare.bibleplanner.core.date.LocalDateTimeProvider
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterRef
import com.quare.bibleplanner.core.verseannotations.domain.model.AnnotatedPassage
import com.quare.bibleplanner.core.verseannotations.domain.model.HighlightColor
import com.quare.bibleplanner.core.verseannotations.domain.model.PresetHighlightColor
import com.quare.bibleplanner.core.verseannotations.domain.model.VerseNote
import com.quare.bibleplanner.feature.verse.annotations.domain.model.AnnotationEntry
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

internal const val SAMPLE_VERSION_ID = "arc"
private const val MILLIS_PER_DAY = 86_400_000L

internal val sampleNow: Long = LocalDateTime(
    year = 2026,
    month = 9,
    day = 26,
    hour = 12,
    minute = 0,
).toInstant(TimeZone.UTC).toEpochMilliseconds()

internal val utcLocalDateTimeProvider = LocalDateTimeProvider { timestamp ->
    Instant.fromEpochMilliseconds(timestamp).toLocalDateTime(TimeZone.UTC)
}

internal val yellow = HighlightColor.Preset(PresetHighlightColor.YELLOW)

internal fun daysAgo(days: Int): Long = sampleNow - days * MILLIS_PER_DAY

internal fun samplePassage(
    bookId: BookId = BookId.GEN,
    chapterNumber: Int = 1,
    verseNumbers: List<Int> = listOf(1),
    highlightColor: HighlightColor? = null,
    isSaved: Boolean = false,
    noteText: String? = null,
    updatedAt: Long = sampleNow,
): AnnotatedPassage {
    val chapter = ChapterRef(
        bibleVersionId = SAMPLE_VERSION_ID,
        bookId = bookId,
        chapterNumber = chapterNumber,
    )
    return AnnotatedPassage(
        chapter = chapter,
        verseNumbers = verseNumbers,
        highlightColor = highlightColor,
        isSaved = isSaved,
        note = noteText?.let { text ->
            VerseNote(
                id = "note-${bookId.name}-$chapterNumber-${verseNumbers.first()}",
                chapter = chapter,
                verseNumbers = verseNumbers,
                text = text,
                createdAtEpochMillis = updatedAt,
                updatedAtEpochMillis = updatedAt,
            )
        },
        updatedAtEpochMillis = updatedAt,
    )
}

internal fun AnnotatedPassage.toEntry(
    text: String = "In the beginning",
    reference: String = "${chapter.bookId.name} ${chapter.chapterNumber}:${verseNumbers.joinToString(separator = ",")}",
): AnnotationEntry = AnnotationEntry(
    passage = this,
    text = text,
    reference = reference,
)
