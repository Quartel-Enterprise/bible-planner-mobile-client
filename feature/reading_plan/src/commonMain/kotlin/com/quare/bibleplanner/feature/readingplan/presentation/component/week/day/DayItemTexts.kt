package com.quare.bibleplanner.feature.readingplan.presentation.component.week.day

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import bibleplanner.feature.reading_plan.generated.resources.Res
import bibleplanner.feature.reading_plan.generated.resources.day_number
import com.quare.bibleplanner.core.books.util.getBookName
import com.quare.bibleplanner.core.model.plan.DayModel
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.utils.SharedTransitionAnimationUtils
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun SharedTransitionScope.DayItemTexts(
    modifier: Modifier = Modifier,
    day: DayModel,
    weekNumber: Int,
    animatedContentScope: AnimatedContentScope,
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
            MaterialTheme.colorScheme.onSurface
        },
        label = "dayPassageColor",
    )
    val dayNumber = day.number
    Column(modifier = modifier) {
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
        Text(
            text = day.passages.toDayReadingFormat(),
            style = MaterialTheme.typography.bodyMedium,
            color = passageColor,
            textDecoration = if (day.isRead) {
                TextDecoration.LineThrough
            } else {
                TextDecoration.None
            },
        )
    }
}

@Composable
private fun List<PassageModel>.toDayReadingFormat(): String {
    val passagesPresentation = map { passage ->
        passage.run {
            val bookName = passage.bookId.getBookName()
            // If no chapters, show just the book name (e.g., "Obadiah")
            // Otherwise show book name with chapter ranges (e.g., "2 Samuel 5:1-10")
            if (chapterRanges.orEmpty().isEmpty()) {
                bookName
            } else {
                "$bookName $chapterRanges"
            }
        }
    }
    return passagesPresentation.joinToString(", ")
}
