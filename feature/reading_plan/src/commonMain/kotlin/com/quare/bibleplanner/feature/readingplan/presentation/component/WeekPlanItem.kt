package com.quare.bibleplanner.feature.readingplan.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import bibleplanner.feature.reading_plan.generated.resources.Res
import bibleplanner.feature.reading_plan.generated.resources.day_number
import bibleplanner.feature.reading_plan.generated.resources.week_complete
import com.quare.bibleplanner.core.model.plan.ChapterPlanModel
import com.quare.bibleplanner.core.model.plan.DayModel
import com.quare.bibleplanner.core.model.plan.PassagePlanModel
import com.quare.bibleplanner.feature.readingplan.presentation.model.WeekPlanPresentationModel
import com.quare.bibleplanner.feature.readingplan.presentation.util.getBookName
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun WeekPlanItem(
    modifier: Modifier = Modifier,
    weekPresentation: WeekPlanPresentationModel,
    onExpandClick: () -> Unit,
) {
    val isExpanded = weekPresentation.isExpanded
    val readDaysCount = weekPresentation.readDaysCount
    val totalDays = weekPresentation.totalDays
    val week = weekPresentation.weekPlan

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onExpandClick)
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(
                    Res.string.week_complete,
                    week.number,
                    readDaysCount,
                    totalDays,
                ),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
            )
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                modifier = Modifier.size(24.dp),
            )
        }

        if (isExpanded) {
            week.days.forEach { day ->
                DayItem(
                    day = day,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
    }
}

@Composable
private fun DayItem(
    day: DayModel,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(Res.string.day_number, day.number),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
                val dayReadingText = formatDayReading(day.passages)
                Text(
                    text = dayReadingText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }

            Checkbox(
                checked = day.isRead,
                onCheckedChange = null,
                enabled = false,
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
