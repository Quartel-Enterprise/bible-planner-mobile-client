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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.quare.bibleplanner.core.model.plan.WeekPlanModel
import com.quare.bibleplanner.feature.readingplan.presentation.util.getBookName
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun WeekPlanItem(
    modifier: Modifier = Modifier,
    week: WeekPlanModel,
) {
    var isExpanded by remember(week.number) { mutableStateOf(week.number == 1) }
    val readDaysCount = week.days.count { it.isRead }
    val totalDays = week.days.size

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.Center) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
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
        passageTexts.add("$bookName $chapterRanges")
    }
    return passageTexts.joinToString(", ")
}

private fun formatChapterRanges(chapters: List<ChapterPlanModel>): String {
    if (chapters.isEmpty()) return ""

    val chapterNumbers = chapters.map { it.number }
    val ranges = mutableListOf<String>()

    var start: Int? = null
    var end: Int? = null

    for (chapterNumber in chapterNumbers.sorted()) {
        when {
            start == null -> {
                start = chapterNumber
                end = chapterNumber
            }

            chapterNumber == end!! + 1 -> {
                end = chapterNumber
            }

            else -> {
                ranges.add(formatRange(start, end))
                start = chapterNumber
                end = chapterNumber
            }
        }
    }
    if (start != null) {
        ranges.add(formatRange(start, end))
    }

    return ranges.joinToString(", ")
}

private fun formatRange(
    start: Int,
    end: Int?,
): String = when {
    end == null || start == end -> "$start"
    else -> "$start-$end"
}
