package com.quare.bibleplanner.feature.dayreadingcomplete.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bibleplanner.feature.day_reading_complete.generated.resources.Res
import bibleplanner.feature.day_reading_complete.generated.resources.day_reading_complete_body_early
import bibleplanner.feature.day_reading_complete.generated.resources.day_reading_complete_body_on_time
import bibleplanner.feature.day_reading_complete.generated.resources.day_reading_complete_body_overdue
import bibleplanner.feature.day_reading_complete.generated.resources.day_reading_complete_card_subtitle
import bibleplanner.feature.day_reading_complete.generated.resources.day_reading_complete_card_title_other_day
import bibleplanner.feature.day_reading_complete.generated.resources.day_reading_complete_card_title_today
import bibleplanner.feature.day_reading_complete.generated.resources.day_reading_complete_close
import bibleplanner.feature.day_reading_complete.generated.resources.day_reading_complete_cta_generate_other_day
import bibleplanner.feature.day_reading_complete.generated.resources.day_reading_complete_cta_generate_today
import bibleplanner.feature.day_reading_complete.generated.resources.day_reading_complete_cta_subscribe
import bibleplanner.feature.day_reading_complete.generated.resources.day_reading_complete_cta_view_other_day
import bibleplanner.feature.day_reading_complete.generated.resources.day_reading_complete_cta_view_today
import bibleplanner.feature.day_reading_complete.generated.resources.day_reading_complete_day_label
import bibleplanner.feature.day_reading_complete.generated.resources.day_reading_complete_dismiss_default
import bibleplanner.feature.day_reading_complete.generated.resources.day_reading_complete_dismiss_exhausted
import bibleplanner.feature.day_reading_complete.generated.resources.day_reading_complete_kicker_other_day
import bibleplanner.feature.day_reading_complete.generated.resources.day_reading_complete_kicker_today
import bibleplanner.feature.day_reading_complete.generated.resources.day_reading_complete_quota_exhausted
import bibleplanner.feature.day_reading_complete.generated.resources.day_reading_complete_quota_hint
import bibleplanner.feature.day_reading_complete.generated.resources.day_reading_complete_title_early
import bibleplanner.feature.day_reading_complete.generated.resources.day_reading_complete_title_on_time
import bibleplanner.feature.day_reading_complete.generated.resources.day_reading_complete_title_overdue
import com.quare.bibleplanner.core.books.util.toReadingLabel
import com.quare.bibleplanner.feature.dayreadingcomplete.domain.model.DayTimingState
import com.quare.bibleplanner.feature.dayreadingcomplete.domain.model.StudyCtaState
import com.quare.bibleplanner.feature.dayreadingcomplete.presentation.model.DayReadingCompleteUiEvent
import com.quare.bibleplanner.feature.dayreadingcomplete.presentation.model.DayReadingCompleteUiState
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer
import com.quare.bibleplanner.ui.utils.toStringResource
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

private val checkIconSize = 64.dp
private val checkIconInnerSize = 34.dp
private val cardIconSize = 40.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DayReadingCompleteBottomSheet(
    uiState: DayReadingCompleteUiState,
    onEvent: (DayReadingCompleteUiEvent) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = { onEvent(DayReadingCompleteUiEvent.OnDismiss) },
        sheetState = sheetState,
    ) {
        when (uiState) {
            DayReadingCompleteUiState.Loading -> LoadingContent()
            is DayReadingCompleteUiState.Loaded -> LoadedContent(uiState, onEvent)
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun LoadedContent(
    state: DayReadingCompleteUiState.Loaded,
    onEvent: (DayReadingCompleteUiEvent) -> Unit,
) {
    val isToday = state.timing == DayTimingState.ON_TIME
    val dayLabel = state.plannedReadDate?.takeIf { !isToday }?.toDayLabel()
    val readingLabel = state.passages.toReadingLabel()

    Box(modifier = Modifier.fillMaxWidth()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(top = 8.dp, bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            CheckIcon()
            VerticalSpacer(20.dp)
            Text(
                text = kickerText(state.timing, dayLabel),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
            )
            VerticalSpacer(8.dp)
            Text(
                text = titleText(state, readingLabel, dayLabel),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
            )
            VerticalSpacer(12.dp)
            Text(
                text = bodyText(state, dayLabel),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            VerticalSpacer(24.dp)
            StudyCard(state = state, isToday = isToday, dayLabel = dayLabel, readingLabel = readingLabel)
            VerticalSpacer(20.dp)
            Button(
                onClick = { onEvent(DayReadingCompleteUiEvent.OnCtaClick(readingLabel)) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = ctaText(state.ctaState, isToday))
            }
            hintText(state.ctaState)?.let { hint ->
                VerticalSpacer(8.dp)
                Text(
                    text = hint,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            VerticalSpacer(12.dp)
            TextButton(onClick = { onEvent(DayReadingCompleteUiEvent.OnDismiss) }) {
                Text(text = dismissText(state.ctaState))
            }
        }
        Icon(
            imageVector = Icons.Rounded.Close,
            contentDescription = stringResource(Res.string.day_reading_complete_close),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 8.dp, end = 8.dp),
        )
    }
}

@Composable
private fun CheckIcon() {
    Box(
        modifier = Modifier
            .size(checkIconSize)
            .background(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(checkIconInnerSize),
            tint = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun StudyCard(
    state: DayReadingCompleteUiState.Loaded,
    isToday: Boolean,
    dayLabel: String?,
    readingLabel: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(cardIconSize)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = cardTitleText(isToday, dayLabel),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = pluralStringResource(
                        Res.plurals.day_reading_complete_card_subtitle,
                        state.chapterCount,
                        readingLabel,
                        state.chapterCount,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun kickerText(
    timing: DayTimingState,
    dayLabel: String?,
): String = if (timing == DayTimingState.ON_TIME || dayLabel == null) {
    stringResource(Res.string.day_reading_complete_kicker_today)
} else {
    stringResource(Res.string.day_reading_complete_kicker_other_day, dayLabel.uppercase())
}

@Composable
private fun titleText(
    state: DayReadingCompleteUiState.Loaded,
    readingLabel: String,
    dayLabel: String?,
): String = when {
    state.timing == DayTimingState.ON_TIME || dayLabel == null ->
        stringResource(Res.string.day_reading_complete_title_on_time, readingLabel)

    state.timing == DayTimingState.OVERDUE ->
        stringResource(Res.string.day_reading_complete_title_overdue, dayLabel)

    else -> stringResource(Res.string.day_reading_complete_title_early, dayLabel)
}

@Composable
private fun bodyText(
    state: DayReadingCompleteUiState.Loaded,
    dayLabel: String?,
): String {
    val count = state.chapterCount
    return when {
        state.timing == DayTimingState.ON_TIME || dayLabel == null ->
            pluralStringResource(Res.plurals.day_reading_complete_body_on_time, count, count)

        state.timing == DayTimingState.OVERDUE ->
            pluralStringResource(Res.plurals.day_reading_complete_body_overdue, count, count, dayLabel)

        else -> pluralStringResource(Res.plurals.day_reading_complete_body_early, count, count, dayLabel)
    }
}

@Composable
private fun cardTitleText(
    isToday: Boolean,
    dayLabel: String?,
): String = if (isToday || dayLabel == null) {
    stringResource(Res.string.day_reading_complete_card_title_today)
} else {
    stringResource(Res.string.day_reading_complete_card_title_other_day, dayLabel)
}

@Composable
private fun ctaText(
    ctaState: StudyCtaState,
    isToday: Boolean,
): String = when (ctaState) {
    is StudyCtaState.FreeExhausted -> stringResource(Res.string.day_reading_complete_cta_subscribe)

    is StudyCtaState.FreeWithQuota -> if (isToday) {
        stringResource(Res.string.day_reading_complete_cta_generate_today)
    } else {
        stringResource(Res.string.day_reading_complete_cta_generate_other_day)
    }

    StudyCtaState.Pro -> if (isToday) {
        stringResource(Res.string.day_reading_complete_cta_view_today)
    } else {
        stringResource(Res.string.day_reading_complete_cta_view_other_day)
    }
}

@Composable
private fun hintText(ctaState: StudyCtaState): String? = when (ctaState) {
    is StudyCtaState.FreeWithQuota -> pluralStringResource(
        Res.plurals.day_reading_complete_quota_hint,
        ctaState.remaining,
        ctaState.remaining,
        ctaState.limit,
    )

    is StudyCtaState.FreeExhausted -> pluralStringResource(
        Res.plurals.day_reading_complete_quota_exhausted,
        ctaState.limit,
        ctaState.limit,
    )

    StudyCtaState.Pro -> null
}

@Composable
private fun dismissText(ctaState: StudyCtaState): String = if (ctaState is StudyCtaState.FreeExhausted) {
    stringResource(Res.string.day_reading_complete_dismiss_exhausted)
} else {
    stringResource(Res.string.day_reading_complete_dismiss_default)
}

@Composable
private fun LocalDate.toDayLabel(): String {
    val monthName = stringResource(month.toStringResource()).lowercase()
    return stringResource(Res.string.day_reading_complete_day_label, day, monthName)
}
