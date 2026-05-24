package com.quare.bibleplanner.feature.readingplan.presentation.component.week.day

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import bibleplanner.feature.reading_plan.generated.resources.today_badge
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
    isNextToRead: Boolean,
    animatedContentScope: AnimatedContentScope,
) {
    val isRead = day.isRead
    val isToday = day.isToday
    val titleColor by animateColorAsState(
        targetValue = when {
            isRead -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            isNextToRead -> MaterialTheme.colorScheme.onPrimaryContainer
            else -> MaterialTheme.colorScheme.onSurface
        },
        label = "dayTitleColor",
    )
    val passageColor by animateColorAsState(
        targetValue = when {
            isRead -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
            isNextToRead -> MaterialTheme.colorScheme.onPrimaryContainer
            else -> MaterialTheme.colorScheme.onSurface
        },
        label = "dayPassageColor",
    )
    val textDecoration = if (isRead) TextDecoration.LineThrough else TextDecoration.None
    val dayNumber = day.number
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                modifier = Modifier.sharedElement(
                    sharedContentState = rememberSharedContentState(
                        key = SharedTransitionAnimationUtils.buildDayNumberId(weekNumber, dayNumber),
                    ),
                    animatedVisibilityScope = animatedContentScope,
                ),
                text = stringResource(Res.string.day_number, dayNumber),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = titleColor,
                textDecoration = textDecoration,
            )
            if (isToday) {
                TodayBadge()
            }
        }
        Text(
            text = day.passages.toDayReadingFormat(),
            style = MaterialTheme.typography.bodyMedium,
            color = passageColor,
            textDecoration = textDecoration,
        )
    }
}

@Composable
private fun TodayBadge() {
    Surface(
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = RoundedCornerShape(50),
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
            text = stringResource(Res.string.today_badge),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun List<PassageModel>.toDayReadingFormat(): String {
    val passagesPresentation = map { passage ->
        passage.run {
            val bookName = passage.bookId.getBookName()
            if (chapterRanges.orEmpty().isEmpty()) {
                bookName
            } else {
                "$bookName $chapterRanges"
            }
        }
    }
    return passagesPresentation.joinToString(", ")
}
