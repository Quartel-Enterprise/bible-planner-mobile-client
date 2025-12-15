package com.quare.bibleplanner.feature.readingplan.presentation.component

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import bibleplanner.feature.reading_plan.generated.resources.Res
import bibleplanner.feature.reading_plan.generated.resources.day_number
import com.quare.bibleplanner.core.books.util.getBookName
import com.quare.bibleplanner.core.model.plan.ChapterPlanModel
import com.quare.bibleplanner.core.model.plan.DayModel
import com.quare.bibleplanner.core.model.plan.PassagePlanModel
import com.quare.bibleplanner.core.utils.SharedTransitionAnimationUtils
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiEvent
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun SharedTransitionScope.DayItem(
    animatedContentScope: AnimatedContentScope,
    weekNumber: Int,
    day: DayModel,
    modifier: Modifier = Modifier,
    onEvent: (ReadingPlanUiEvent) -> Unit,
) {
    val titleColor by animateColorAsState(
        targetValue = if (day.isRead) {
            MaterialTheme.colorScheme.onSurfaceVariant
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        label = "dayTitleColor",
    )
    val passageColor by animateColorAsState(
        targetValue = if (day.isRead) {
            MaterialTheme.colorScheme.onSurfaceVariant
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        label = "dayPassageColor",
    )
    val dayNumber = day.number
    Column(
        modifier = modifier
            .clickable {
                onEvent(
                    ReadingPlanUiEvent.OnDayClick(
                        dayNumber = dayNumber,
                        weekNumber = weekNumber,
                    ),
                )
            }.padding(vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(Res.string.day_number, dayNumber),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = titleColor,
                    textDecoration = if (day.isRead) {
                        TextDecoration.LineThrough
                    } else {
                        TextDecoration.None
                    },
                    modifier = Modifier.sharedElement(
                        sharedContentState = rememberSharedContentState(
                            key = SharedTransitionAnimationUtils.buildDayNumberId(weekNumber, dayNumber),
                        ),
                        animatedVisibilityScope = animatedContentScope,
                    ),
                )
                val dayReadingText = formatDayReading(day.passages)
                Text(
                    text = dayReadingText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = passageColor,
                    modifier = Modifier.padding(top = 2.dp),
                    textDecoration = if (day.isRead) {
                        TextDecoration.LineThrough
                    } else {
                        TextDecoration.None
                    },
                )
            }

            Checkbox(
                checked = day.isRead,
                onCheckedChange = {
                    onEvent(
                        ReadingPlanUiEvent.OnDayReadClick(
                            dayNumber = dayNumber,
                            weekNumber = weekNumber,
                        ),
                    )
                },
            )
        }
    }
}

@Composable
private fun formatDayReading(passages: List<PassagePlanModel>): String {
    val passageTexts = mutableListOf<String>()
    for (passage in passages) {
        val bookName = passage.bookId.getBookName()
        val chapterRanges = formatChapterRanges(passage.chapters)
        // If no chapters, show just the book name (e.g., "Obadiah")
        // Otherwise show book name with chapter ranges (e.g., "2 Samuel 5:1-10")
        val passageText = if (chapterRanges.isEmpty()) {
            bookName
        } else {
            "$bookName $chapterRanges"
        }
        passageTexts.add(passageText)
    }
    return passageTexts.joinToString(", ")
}

private fun formatChapterRanges(chapters: List<ChapterPlanModel>): String {
    if (chapters.isEmpty()) return ""

    val sortedChapters = chapters.sortedBy { it.number }
    val ranges = mutableListOf<String>()

    var currentRangeStart: ChapterPlanModel? = null
    var currentRangeEnd: ChapterPlanModel? = null

    for (chapter in sortedChapters) {
        when {
            currentRangeStart == null -> {
                // Start a new range
                currentRangeStart = chapter
                currentRangeEnd = chapter
            }

            canGroupChapters(currentRangeEnd!!, chapter) -> {
                // Extend the current range
                currentRangeEnd = chapter
            }

            else -> {
                // Finish current range and start a new one
                ranges.add(formatChapterRange(currentRangeStart, currentRangeEnd))
                currentRangeStart = chapter
                currentRangeEnd = chapter
            }
        }
    }

    // Add the last range
    if (currentRangeStart != null) {
        ranges.add(formatChapterRange(currentRangeStart, currentRangeEnd))
    }

    return ranges.joinToString(", ")
}

private fun canGroupChapters(
    first: ChapterPlanModel,
    second: ChapterPlanModel,
): Boolean {
    // Can only group if chapters are consecutive
    if (second.number != first.number + 1) return false

    // Can group if neither has specific verse ranges
    val firstHasVerses = first.startVerse != null || first.endVerse != null
    val secondHasVerses = second.startVerse != null || second.endVerse != null

    // Only group if both don't have verses (full chapters)
    return !firstHasVerses && !secondHasVerses
}

private fun formatChapterRange(
    start: ChapterPlanModel,
    end: ChapterPlanModel?,
): String {
    val endChapter = end ?: start
    val startVerseStr = formatVerseRange(start.startVerse, start.endVerse)
    val endVerseStr = formatVerseRange(endChapter.startVerse, endChapter.endVerse)

    return when {
        start.number == endChapter.number -> {
            // Single chapter
            if (startVerseStr != null) {
                "${start.number}:$startVerseStr"
            } else {
                "${start.number}"
            }
        }

        startVerseStr != null && endVerseStr != null -> {
            // Range with verses: "5:1-10-12:5-8"
            "${start.number}:$startVerseStr-${endChapter.number}:$endVerseStr"
        }

        startVerseStr != null -> {
            // Start has verses, end doesn't: "5:1-10-12"
            "${start.number}:$startVerseStr-${endChapter.number}"
        }

        endVerseStr != null -> {
            // End has verses, start doesn't: "5-12:5-8"
            "${start.number}-${endChapter.number}:$endVerseStr"
        }

        else -> {
            // No verses: "5-12"
            "${start.number}-${endChapter.number}"
        }
    }
}

private fun formatVerseRange(
    startVerse: Int?,
    endVerse: Int?,
): String? = when {
    startVerse != null && endVerse != null -> {
        if (startVerse == endVerse) {
            "$startVerse"
        } else {
            "$startVerse-$endVerse"
        }
    }

    startVerse != null -> {
        "$startVerse"
    }

    endVerse != null -> {
        "-$endVerse"
    }

    else -> {
        null
    }
}
