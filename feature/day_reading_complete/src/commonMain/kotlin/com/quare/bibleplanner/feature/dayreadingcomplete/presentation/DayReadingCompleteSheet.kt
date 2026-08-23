package com.quare.bibleplanner.feature.dayreadingcomplete.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bibleplanner.feature.day_reading_complete.generated.resources.Res
import bibleplanner.feature.day_reading_complete.generated.resources.day_reading_complete_body_early
import bibleplanner.feature.day_reading_complete.generated.resources.day_reading_complete_body_on_time
import bibleplanner.feature.day_reading_complete.generated.resources.day_reading_complete_body_overdue
import bibleplanner.feature.day_reading_complete.generated.resources.day_reading_complete_card_subtitle
import bibleplanner.feature.day_reading_complete.generated.resources.day_reading_complete_card_title_other_day
import bibleplanner.feature.day_reading_complete.generated.resources.day_reading_complete_card_title_today
import bibleplanner.feature.day_reading_complete.generated.resources.day_reading_complete_cta_generate_other_day
import bibleplanner.feature.day_reading_complete.generated.resources.day_reading_complete_cta_generate_today
import bibleplanner.feature.day_reading_complete.generated.resources.day_reading_complete_cta_subscribe
import bibleplanner.feature.day_reading_complete.generated.resources.day_reading_complete_cta_view_other_day
import bibleplanner.feature.day_reading_complete.generated.resources.day_reading_complete_cta_view_today
import bibleplanner.feature.day_reading_complete.generated.resources.day_reading_complete_day_label
import bibleplanner.feature.day_reading_complete.generated.resources.day_reading_complete_kicker_other_day
import bibleplanner.feature.day_reading_complete.generated.resources.day_reading_complete_kicker_today
import bibleplanner.feature.day_reading_complete.generated.resources.day_reading_complete_quota_exhausted
import bibleplanner.feature.day_reading_complete.generated.resources.day_reading_complete_quota_hint
import bibleplanner.feature.day_reading_complete.generated.resources.day_reading_complete_title_early
import bibleplanner.feature.day_reading_complete.generated.resources.day_reading_complete_title_on_time
import bibleplanner.feature.day_reading_complete.generated.resources.day_reading_complete_title_overdue
import com.quare.bibleplanner.core.books.util.toReadingLabel
import com.quare.bibleplanner.core.model.loadable.valueOrNull
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.dayreadingcomplete.domain.model.DayTimingState
import com.quare.bibleplanner.feature.dayreadingcomplete.domain.model.StudyCtaState
import com.quare.bibleplanner.feature.dayreadingcomplete.presentation.model.DayReadingCompleteUiEvent
import com.quare.bibleplanner.feature.dayreadingcomplete.presentation.model.DayReadingCompleteUiState
import com.quare.bibleplanner.ui.component.shimmer.ShimmerBox
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer
import com.quare.bibleplanner.ui.utils.toStringResource
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

private val checkIconBoxSize = 52.dp
private val checkIconSize = 28.dp
private val cardIconSize = 22.dp
private val cardCornerRadius = 16.dp
private val hintIconSize = 14.dp
private val bodyMaxWidth = 310.dp
private val ctaHeight = 50.dp
private val eyebrowShimmerWidth = 190.dp
private val eyebrowShimmerHeight = 12.dp
private val titleShimmerWidth = 240.dp
private val titleShimmerHeight = 20.dp
private val bodyShimmerHeight = 14.dp
private val cardShimmerHeight = 72.dp
private val hintShimmerWidth = 210.dp
private val hintShimmerHeight = 12.dp
private val bodyShimmerWidthFractions = listOf(1f, 0.94f, 0.62f)
private val titleFontSize = 19.sp
private val eyebrowLetterSpacing = 0.5.sp
private const val SOFT_PRIMARY_ALPHA = 0.12f

@Composable
internal fun DayReadingCompleteSheet(
    uiState: DayReadingCompleteUiState,
    onEvent: (DayReadingCompleteUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        DayReadingCompleteUiState.Loading -> LoadingContent(modifier)

        is DayReadingCompleteUiState.Loaded -> LoadedContent(
            state = uiState,
            onEvent = onEvent,
            modifier = modifier,
        )
    }
}

/** The loaded layout with the copy taken out, so nothing moves when the day arrives. */
@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    SheetColumn(modifier = modifier) {
        ShimmerBox(
            modifier = Modifier.size(checkIconBoxSize),
            shape = CircleShape,
        )
        VerticalSpacer(12)
        ShimmerLine(width = eyebrowShimmerWidth, height = eyebrowShimmerHeight)
        VerticalSpacer(8)
        ShimmerLine(width = titleShimmerWidth, height = titleShimmerHeight)
        VerticalSpacer(12)
        bodyShimmerWidthFractions.forEachIndexed { index, fraction ->
            if (index > 0) VerticalSpacer(8)
            ShimmerBox(
                modifier = Modifier
                    .widthIn(max = bodyMaxWidth)
                    .fillMaxWidth(fraction)
                    .height(bodyShimmerHeight),
            )
        }
        VerticalSpacer(20)
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(cardShimmerHeight),
            shape = RoundedCornerShape(cardCornerRadius),
        )
        VerticalSpacer(14)
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(ctaHeight),
            shape = CircleShape,
        )
        VerticalSpacer(14)
        ShimmerLine(width = hintShimmerWidth, height = hintShimmerHeight)
    }
}

@Composable
private fun ShimmerLine(
    width: Dp,
    height: Dp,
) {
    ShimmerBox(
        modifier = Modifier
            .width(width)
            .height(height),
    )
}

@Composable
private fun SheetColumn(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp)
            .padding(top = 12.dp, bottom = 26.dp),
        content = content,
    )
}

@Composable
private fun LoadedContent(
    state: DayReadingCompleteUiState.Loaded,
    onEvent: (DayReadingCompleteUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isToday = state.timing == DayTimingState.ON_TIME
    val dayLabel = state.plannedReadDate?.takeIf { !isToday }?.toDayLabel(state.language)
    val readingLabel = state.passages.toReadingLabel()

    SheetColumn(modifier = modifier) {
        CheckIcon()
        VerticalSpacer(12)
        Text(
            text = kickerText(state.timing, dayLabel),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            letterSpacing = eyebrowLetterSpacing,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
        )
        VerticalSpacer(6)
        Text(
            text = titleText(state, readingLabel, dayLabel),
            style = MaterialTheme.typography.titleLarge,
            fontSize = titleFontSize,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
        )
        VerticalSpacer(8)
        Text(
            modifier = Modifier.widthIn(max = bodyMaxWidth),
            text = bodyText(state, dayLabel),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        VerticalSpacer(16)
        StudyCard(
            state = state,
            isToday = isToday,
            dayLabel = dayLabel,
            readingLabel = readingLabel,
        )
        VerticalSpacer(14)
        val ctaState = state.ctaState.valueOrNull()
        if (ctaState == null) {
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ctaHeight),
                shape = CircleShape,
            )
        } else {
            Button(
                onClick = { onEvent(DayReadingCompleteUiEvent.OnCtaClick(readingLabel)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ctaHeight),
            ) {
                Icon(
                    modifier = Modifier.size(ButtonDefaults.IconSize),
                    imageVector = ctaState.toIcon(),
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                Text(text = ctaText(ctaState, isToday))
            }
        }
        hintText(ctaState)?.let { hint ->
            VerticalSpacer(10)
            QuotaHint(hint)
        }
    }
}

@Composable
private fun CheckIcon() {
    Box(
        modifier = Modifier
            .size(checkIconBoxSize)
            .background(
                color = MaterialTheme.colorScheme.primary.copy(alpha = SOFT_PRIMARY_ALPHA),
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.TaskAlt,
            contentDescription = null,
            modifier = Modifier.size(checkIconSize),
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(cardCornerRadius))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Rounded.AutoAwesome,
            contentDescription = null,
            modifier = Modifier.size(cardIconSize),
            tint = MaterialTheme.colorScheme.primary,
        )
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

@Composable
private fun QuotaHint(hint: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Rounded.Bolt,
            contentDescription = null,
            modifier = Modifier.size(hintIconSize),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = hint,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
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

private fun StudyCtaState.toIcon(): ImageVector = if (this is StudyCtaState.FreeExhausted) {
    Icons.Rounded.WorkspacePremium
} else {
    Icons.Rounded.AutoAwesome
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
private fun hintText(ctaState: StudyCtaState?): String? = when (ctaState) {
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

    StudyCtaState.Pro, null -> null
}

@Composable
private fun LocalDate.toDayLabel(language: Language): String {
    val rawMonthName = stringResource(month.toStringResource())
    val monthName = if (language == Language.ENGLISH) rawMonthName else rawMonthName.lowercase()
    return stringResource(Res.string.day_reading_complete_day_label, day, monthName)
}
