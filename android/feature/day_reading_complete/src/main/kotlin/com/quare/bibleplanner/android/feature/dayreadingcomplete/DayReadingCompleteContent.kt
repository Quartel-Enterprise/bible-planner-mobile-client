package com.quare.bibleplanner.android.feature.dayreadingcomplete

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.android.designsystem.icon.SheetIconTile
import com.quare.bibleplanner.core.books.util.toReadingLabel
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.dayreadingcomplete.domain.model.DayTimingState
import com.quare.bibleplanner.feature.dayreadingcomplete.domain.model.StudyCtaState
import com.quare.bibleplanner.feature.dayreadingcomplete.presentation.model.DayReadingCompleteUiEvent
import com.quare.bibleplanner.feature.dayreadingcomplete.presentation.model.DayReadingCompleteUiState
import com.quare.bibleplanner.ui.utils.sheet.SheetCloseGuard
import com.quare.bibleplanner.ui.utils.sheet.SheetExitAnimationEffect
import com.quare.bibleplanner.ui.utils.sheet.blockPointerInput
import com.quare.bibleplanner.ui.utils.sheet.rememberSheetCloseGuard
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import software.quare.loadable.valueOrNull
import java.time.format.TextStyle
import java.util.Locale

private val iconTileSize = 56.dp
private val cardIconSize = 22.dp
private val hintIconSize = 14.dp
private val bodyMaxWidth = 310.dp
private val ctaHeight = 50.dp
private val eyebrowSkeletonWidth = 190.dp
private val eyebrowSkeletonHeight = 12.dp
private val titleSkeletonWidth = 240.dp
private val titleSkeletonHeight = 20.dp
private val bodySkeletonHeight = 14.dp
private val cardSkeletonHeight = 72.dp
private val hintSkeletonWidth = 210.dp
private val hintSkeletonHeight = 12.dp
private val bodySkeletonWidthFractions = listOf(1f, 0.94f, 0.62f)
private const val PORTUGUESE_BRAZIL_TAG = "pt-BR"
private const val SPANISH_TAG = "es"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DayReadingCompleteContent(
    uiState: DayReadingCompleteUiState,
    onEvent: (DayReadingCompleteUiEvent) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val sheetCloseGuard = rememberSheetCloseGuard()
    val onDismiss = { sheetCloseGuard.close { onEvent(DayReadingCompleteUiEvent.OnDismiss) } }
    SheetExitAnimationEffect(
        animateOut = sheetState::hide,
        animateBackIn = sheetState::show,
    )
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        sheetGesturesEnabled = !sheetCloseGuard.isClosing,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .blockPointerInput(isBlocked = sheetCloseGuard.isClosing)
                .fillMaxWidth()
                .padding(
                    start = 24.dp,
                    end = 24.dp,
                    bottom = 16.dp,
                ).verticalScroll(rememberScrollState()),
        ) {
            when (uiState) {
                DayReadingCompleteUiState.Loading -> LoadingContent()

                is DayReadingCompleteUiState.Loaded -> LoadedContent(
                    state = uiState,
                    sheetCloseGuard = sheetCloseGuard,
                    onEvent = onEvent,
                )
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    val shimmer = rememberShimmer(shimmerBounds = ShimmerBounds.Window)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .shimmer(customShimmer = shimmer),
    ) {
        SkeletonBox(
            modifier = Modifier.size(iconTileSize),
            shape = MaterialTheme.shapes.large,
        )
        Spacer(modifier = Modifier.height(12.dp))
        SkeletonLine(
            width = eyebrowSkeletonWidth,
            height = eyebrowSkeletonHeight,
        )
        Spacer(modifier = Modifier.height(8.dp))
        SkeletonLine(
            width = titleSkeletonWidth,
            height = titleSkeletonHeight,
        )
        Spacer(modifier = Modifier.height(12.dp))
        bodySkeletonWidthFractions.forEachIndexed { index, fraction ->
            if (index > 0) Spacer(modifier = Modifier.height(8.dp))
            SkeletonBox(
                modifier = Modifier
                    .widthIn(max = bodyMaxWidth)
                    .fillMaxWidth(fraction)
                    .height(bodySkeletonHeight),
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        SkeletonBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(cardSkeletonHeight),
            shape = MaterialTheme.shapes.large,
        )
        Spacer(modifier = Modifier.height(14.dp))
        CtaSkeleton()
        Spacer(modifier = Modifier.height(14.dp))
        SkeletonLine(
            width = hintSkeletonWidth,
            height = hintSkeletonHeight,
        )
    }
}

@Composable
private fun CtaSkeleton(modifier: Modifier = Modifier) {
    SkeletonBox(
        modifier = modifier
            .fillMaxWidth()
            .height(ctaHeight),
        shape = CircleShape,
    )
}

@Composable
private fun SkeletonLine(
    width: Dp,
    height: Dp,
) {
    SkeletonBox(
        modifier = Modifier
            .width(width)
            .height(height),
    )
}

@Composable
private fun SkeletonBox(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.small,
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
    )
}

@Composable
private fun LoadedContent(
    state: DayReadingCompleteUiState.Loaded,
    sheetCloseGuard: SheetCloseGuard,
    onEvent: (DayReadingCompleteUiEvent) -> Unit,
) {
    val isToday = state.timing == DayTimingState.ON_TIME
    val dayLabel = state.plannedReadDate?.takeIf { !isToday }?.let { date ->
        dayLabelText(
            date = date,
            language = state.language,
        )
    }
    val readingLabel = state.passages.toReadingLabel()

    SheetIconTile(imageVector = Icons.Rounded.TaskAlt)
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = kickerText(
            timing = state.timing,
            dayLabel = dayLabel,
        ),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
        text = titleText(
            timing = state.timing,
            readingLabel = readingLabel,
            dayLabel = dayLabel,
        ),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        modifier = Modifier.widthIn(max = bodyMaxWidth),
        text = bodyText(
            state = state,
            dayLabel = dayLabel,
        ),
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(modifier = Modifier.height(16.dp))
    StudyCard(
        title = cardTitleText(
            isToday = isToday,
            dayLabel = dayLabel,
        ),
        subtitle = pluralStringResource(
            R.plurals.day_reading_complete_card_subtitle,
            state.chapterCount,
            readingLabel,
            state.chapterCount,
        ),
    )
    Spacer(modifier = Modifier.height(14.dp))
    val ctaState = state.ctaState.valueOrNull()
    if (ctaState == null) {
        CtaSkeleton(modifier = Modifier.shimmer())
    } else {
        Button(
            onClick = { sheetCloseGuard.close { onEvent(DayReadingCompleteUiEvent.OnCtaClick(readingLabel)) } },
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
            Text(
                text = ctaText(
                    ctaState = ctaState,
                    isToday = isToday,
                ),
            )
        }
    }
    hintText(ctaState)?.let { hint ->
        Spacer(modifier = Modifier.height(10.dp))
        QuotaHintRow(hint)
    }
    Spacer(modifier = Modifier.height(6.dp))
    TextButton(
        onClick = { sheetCloseGuard.close { onEvent(DayReadingCompleteUiEvent.OnNeverShowAgainClick) } },
    ) {
        Text(text = stringResource(R.string.day_reading_complete_never_show))
    }
}

@Composable
private fun StudyCard(
    title: String,
    subtitle: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(
                horizontal = 14.dp,
                vertical = 12.dp,
            ),
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
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun QuotaHintRow(hint: String) {
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
private fun dayLabelText(
    date: LocalDate,
    language: Language,
): String {
    val monthName = remember(date, language) { date.toMonthName(language) }
    return stringResource(R.string.day_reading_complete_day_label, date.day, monthName)
}

/*
 * Why: a device in a language the app doesn't translate (French, pt-PT) shows the English strings, so the month
 * must follow the app's Language rather than the configuration locale, or it reads "Janvier 5".
 */
private fun LocalDate.toMonthName(language: Language): String {
    val locale = language.toLocale()
    val rawMonthName = toJavaLocalDate().month.getDisplayName(TextStyle.FULL_STANDALONE, locale)
    return if (language == Language.ENGLISH) {
        rawMonthName.replaceFirstChar { char -> char.titlecase(locale) }
    } else {
        rawMonthName.lowercase(locale)
    }
}

private fun Language.toLocale(): Locale = when (this) {
    Language.ENGLISH -> Locale.ENGLISH
    Language.PORTUGUESE_BRAZIL -> Locale.forLanguageTag(PORTUGUESE_BRAZIL_TAG)
    Language.SPANISH -> Locale.forLanguageTag(SPANISH_TAG)
}

@Composable
private fun kickerText(
    timing: DayTimingState,
    dayLabel: String?,
): String = if (timing == DayTimingState.ON_TIME || dayLabel == null) {
    stringResource(R.string.day_reading_complete_kicker_today)
} else {
    stringResource(R.string.day_reading_complete_kicker_other_day, dayLabel.uppercase())
}

@Composable
private fun titleText(
    timing: DayTimingState,
    readingLabel: String,
    dayLabel: String?,
): String = when {
    timing == DayTimingState.ON_TIME || dayLabel == null ->
        stringResource(R.string.day_reading_complete_title_on_time, readingLabel)

    timing == DayTimingState.OVERDUE -> stringResource(R.string.day_reading_complete_title_overdue, dayLabel)

    else -> stringResource(R.string.day_reading_complete_title_early, dayLabel)
}

@Composable
private fun bodyText(
    state: DayReadingCompleteUiState.Loaded,
    dayLabel: String?,
): String {
    val count = state.chapterCount
    return when {
        state.timing == DayTimingState.ON_TIME || dayLabel == null ->
            pluralStringResource(R.plurals.day_reading_complete_body_on_time, count, count)

        state.timing == DayTimingState.OVERDUE ->
            pluralStringResource(R.plurals.day_reading_complete_body_overdue, count, count, dayLabel)

        else -> pluralStringResource(R.plurals.day_reading_complete_body_early, count, count, dayLabel)
    }
}

@Composable
private fun cardTitleText(
    isToday: Boolean,
    dayLabel: String?,
): String = if (isToday || dayLabel == null) {
    stringResource(R.string.day_reading_complete_card_title_today)
} else {
    stringResource(R.string.day_reading_complete_card_title_other_day, dayLabel)
}

private fun StudyCtaState.toIcon(): ImageVector = when {
    this !is StudyCtaState.FreeExhausted -> Icons.Rounded.AutoAwesome
    isRewardedUnlockOffered -> Icons.Rounded.LockOpen
    else -> Icons.Rounded.WorkspacePremium
}

@Composable
private fun ctaText(
    ctaState: StudyCtaState,
    isToday: Boolean,
): String = when (ctaState) {
    is StudyCtaState.FreeExhausted -> if (ctaState.isRewardedUnlockOffered) {
        stringResource(R.string.day_reading_complete_cta_unlock)
    } else {
        stringResource(R.string.day_reading_complete_cta_subscribe)
    }

    is StudyCtaState.FreeWithQuota -> if (isToday) {
        stringResource(R.string.day_reading_complete_cta_generate_today)
    } else {
        stringResource(R.string.day_reading_complete_cta_generate_other_day)
    }

    StudyCtaState.Pro -> if (isToday) {
        stringResource(R.string.day_reading_complete_cta_view_today)
    } else {
        stringResource(R.string.day_reading_complete_cta_view_other_day)
    }
}

@Composable
private fun hintText(ctaState: StudyCtaState?): String? = when (ctaState) {
    is StudyCtaState.FreeWithQuota -> pluralStringResource(
        R.plurals.day_reading_complete_quota_hint,
        ctaState.remaining,
        ctaState.remaining,
        ctaState.limit,
    )

    is StudyCtaState.FreeExhausted -> pluralStringResource(
        R.plurals.day_reading_complete_quota_exhausted,
        ctaState.limit,
        ctaState.limit,
    )

    StudyCtaState.Pro, null -> null
}
