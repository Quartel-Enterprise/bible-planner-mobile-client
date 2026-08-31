package com.quare.bibleplanner.feature.dayreadingcomplete.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import bibleplanner.feature.day_reading_complete.generated.resources.Res
import bibleplanner.feature.day_reading_complete.generated.resources.day_reading_complete_banner_cta_subscribe
import bibleplanner.feature.day_reading_complete.generated.resources.day_reading_complete_banner_title_other_day
import bibleplanner.feature.day_reading_complete.generated.resources.day_reading_complete_banner_title_today
import bibleplanner.feature.day_reading_complete.generated.resources.day_reading_complete_cta_generate_other_day
import bibleplanner.feature.day_reading_complete.generated.resources.day_reading_complete_cta_generate_today
import bibleplanner.feature.day_reading_complete.generated.resources.day_reading_complete_cta_view_other_day
import bibleplanner.feature.day_reading_complete.generated.resources.day_reading_complete_cta_view_today
import com.quare.bibleplanner.core.books.util.toReadingLabel
import com.quare.bibleplanner.core.model.loadable.valueOrNull
import com.quare.bibleplanner.core.model.plan.PlanDayLocationModel
import com.quare.bibleplanner.feature.dayreadingcomplete.domain.model.DayTimingState
import com.quare.bibleplanner.feature.dayreadingcomplete.domain.model.StudyCtaState
import com.quare.bibleplanner.feature.dayreadingcomplete.presentation.model.DayReadingCompleteBannerUiAction
import com.quare.bibleplanner.feature.dayreadingcomplete.presentation.model.DayReadingCompleteBannerUiEvent
import com.quare.bibleplanner.feature.dayreadingcomplete.presentation.model.DayReadingCompleteUiState
import com.quare.bibleplanner.feature.dayreadingcomplete.presentation.viewmodel.DayReadingCompleteBannerViewModel
import com.quare.bibleplanner.ui.component.shimmer.ShimmerBox
import com.quare.bibleplanner.ui.utils.ActionCollector
import com.quare.bibleplanner.ui.utils.AppSnackbarController
import com.quare.bibleplanner.ui.utils.model.AppSnackbarMessage
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private val bannerCornerRadius = 18.dp
private val bannerShadowElevation = 6.dp
private val iconBoxSize = 38.dp
private val iconSize = 20.dp
private val closeIconSize = 18.dp
private val titleShimmerWidth = 180.dp
private val titleShimmerHeight = 13.dp
private val ctaShimmerWidth = 120.dp
private val ctaShimmerHeight = 12.dp
private const val SOFT_PRIMARY_ALPHA = 0.12f

@Composable
fun DayReadingCompleteBanner(
    day: PlanDayLocationModel,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel = koinViewModel<DayReadingCompleteBannerViewModel>(key = day.toString()) {
        parametersOf(day)
    }
    val uiState by viewModel.uiState.collectAsState()

    DayReadingCompleteBannerUiActionCollector(
        viewModel = viewModel,
        onDismissRequest = onDismissRequest,
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(bannerCornerRadius),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant,
        ),
        shadowElevation = bannerShadowElevation,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 14.dp,
                    top = 12.dp,
                    end = 6.dp,
                    bottom = 12.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BannerIcon()
            BannerTexts(
                uiState = uiState,
                onEvent = viewModel::onEvent,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = { viewModel.onEvent(DayReadingCompleteBannerUiEvent.OnDismissClick) }) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = null,
                    modifier = Modifier.size(closeIconSize),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun DayReadingCompleteBannerUiActionCollector(
    viewModel: DayReadingCompleteBannerViewModel,
    onDismissRequest: () -> Unit,
) {
    val appSnackbarController = koinInject<AppSnackbarController>()
    ActionCollector(viewModel.uiAction) { action ->
        when (action) {
            is DayReadingCompleteBannerUiAction.ShowSnackBar -> {
                appSnackbarController.show(
                    AppSnackbarMessage(
                        stringResource = action.message,
                        isDismissible = true,
                    ),
                )
            }

            DayReadingCompleteBannerUiAction.Dismiss -> onDismissRequest()
        }
    }
}

@Composable
private fun BannerIcon() {
    Box(
        modifier = Modifier
            .size(iconBoxSize)
            .background(
                color = MaterialTheme.colorScheme.primary.copy(alpha = SOFT_PRIMARY_ALPHA),
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.TaskAlt,
            contentDescription = null,
            modifier = Modifier.size(iconSize),
            tint = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun BannerTexts(
    uiState: DayReadingCompleteUiState,
    onEvent: (DayReadingCompleteBannerUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        when (uiState) {
            DayReadingCompleteUiState.Loading -> {
                ShimmerBox(
                    modifier = Modifier
                        .width(titleShimmerWidth)
                        .height(titleShimmerHeight),
                )
                ShimmerBox(
                    modifier = Modifier
                        .width(ctaShimmerWidth)
                        .height(ctaShimmerHeight),
                )
            }

            is DayReadingCompleteUiState.Loaded -> LoadedBannerTexts(
                state = uiState,
                onEvent = onEvent,
            )
        }
    }
}

@Composable
private fun LoadedBannerTexts(
    state: DayReadingCompleteUiState.Loaded,
    onEvent: (DayReadingCompleteBannerUiEvent) -> Unit,
) {
    val isToday = state.timing == DayTimingState.ON_TIME
    val dayLabel = state.plannedReadDate?.takeIf { !isToday }?.toDayReadingCompleteDayLabel(state.language)
    val readingLabel = state.passages.toReadingLabel()
    Text(
        text = bannerTitle(
            isToday = isToday,
            dayLabel = dayLabel,
            readingLabel = readingLabel,
        ),
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
    val ctaState = state.ctaState.valueOrNull()
    if (ctaState == null) {
        ShimmerBox(
            modifier = Modifier
                .width(ctaShimmerWidth)
                .height(ctaShimmerHeight),
        )
    } else {
        Text(
            modifier = Modifier.clickable {
                onEvent(DayReadingCompleteBannerUiEvent.OnCtaClick(readingLabel))
            },
            text = bannerCta(
                ctaState = ctaState,
                isToday = isToday,
            ),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun bannerTitle(
    isToday: Boolean,
    dayLabel: String?,
    readingLabel: String,
): String = if (isToday || dayLabel == null) {
    stringResource(
        Res.string.day_reading_complete_banner_title_today,
        readingLabel,
    )
} else {
    stringResource(
        Res.string.day_reading_complete_banner_title_other_day,
        dayLabel,
    )
}

@Composable
private fun bannerCta(
    ctaState: StudyCtaState,
    isToday: Boolean,
): String = when (ctaState) {
    is StudyCtaState.FreeExhausted -> stringResource(Res.string.day_reading_complete_banner_cta_subscribe)

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
