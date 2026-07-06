package com.quare.bibleplanner.feature.day.presentation.content.loading

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.feature.day.presentation.component.DayHeaderTitleSkeleton
import com.quare.bibleplanner.feature.day.presentation.component.DayLandscapeHeader
import com.quare.bibleplanner.feature.day.presentation.model.DayUiEvent
import com.quare.bibleplanner.ui.component.shimmer.ShimmerBox

@Composable
internal fun LoadingDayContent(
    modifier: Modifier,
    isLandscape: Boolean,
    platform: Platform,
    onEvent: (DayUiEvent) -> Unit,
) {
    if (isLandscape) {
        LandscapeSkeleton(
            modifier = modifier,
            platform = platform,
            onEvent = onEvent,
        )
    } else {
        PortraitSkeleton(modifier = modifier)
    }
}

@Composable
private fun PortraitSkeleton(modifier: Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = PortraitContentMaxWidth)
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 8.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            StudyCardSkeleton()
            ButtonSkeleton()
            PassagesCardSkeleton()
            NotesSkeleton()
        }
    }
}

@Composable
private fun LandscapeSkeleton(
    modifier: Modifier,
    platform: Platform,
    onEvent: (DayUiEvent) -> Unit,
) {
    Row(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(READING_WEIGHT)
                .fillMaxHeight(),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = ReadingContentMaxWidth)
                    .fillMaxHeight(),
            ) {
                DayLandscapeHeader(
                    platform = platform,
                    onBackClick = { onEvent(DayUiEvent.OnBackClick) },
                    modifier = Modifier.padding(top = 4.dp),
                ) {
                    DayHeaderTitleSkeleton(horizontalAlignment = Alignment.Start)
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(
                            start = 16.dp,
                            end = 8.dp,
                            top = 8.dp,
                            bottom = 24.dp,
                        ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    PassagesCardSkeleton()
                    ButtonSkeleton()
                    NotesSkeleton()
                }
            }
        }
        VerticalDivider()
        SkeletonColumn(
            weight = STUDY_WEIGHT,
            maxWidth = StudyContentMaxWidth,
            padding = Modifier.padding(20.dp),
        ) {
            StudyPaneSkeleton()
        }
    }
}

@Composable
private fun RowScope.SkeletonColumn(
    weight: Float,
    maxWidth: Dp,
    padding: Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = Modifier
            .weight(weight)
            .fillMaxHeight(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = maxWidth)
                .fillMaxWidth()
                .then(padding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
    }
}

@Composable
private fun StudyCardSkeleton() {
    ShimmerBox(
        modifier = Modifier
            .fillMaxWidth()
            .height(StudyCardHeight),
        shape = RoundedCornerShape(18.dp),
    )
}

@Composable
private fun ButtonSkeleton() {
    ShimmerBox(
        modifier = Modifier
            .fillMaxWidth()
            .height(ButtonHeight),
        shape = RoundedCornerShape(percent = 50),
    )
}

@Composable
private fun PassagesCardSkeleton() {
    ElevatedCard(shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.fillMaxWidth()) {
            repeat(PASSAGE_ROW_COUNT) { index ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ShimmerBox(
                        modifier = Modifier
                            .width(PassageLabelWidth)
                            .height(PassageLabelHeight),
                    )
                    ShimmerBox(
                        modifier = Modifier.size(CheckboxSize),
                        shape = RoundedCornerShape(6.dp),
                    )
                }
                if (index < PASSAGE_ROW_COUNT - 1) {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }
}

@Composable
private fun NotesSkeleton() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ShimmerBox(
            modifier = Modifier
                .width(NotesLabelWidth)
                .height(NotesLabelHeight),
        )
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(NotesFieldHeight),
            shape = RoundedCornerShape(12.dp),
        )
    }
}

@Composable
private fun StudyPaneSkeleton() {
    ShimmerBox(
        modifier = Modifier
            .width(PaneTitleWidth)
            .height(PaneTitleHeight),
    )
    ShimmerBox(
        modifier = Modifier
            .width(PaneSubtitleWidth)
            .height(PaneSubtitleHeight),
    )
    ShimmerBox(
        modifier = Modifier
            .fillMaxWidth()
            .height(PaneTabsHeight),
        shape = RoundedCornerShape(8.dp),
    )
    repeat(PANE_LINE_COUNT) {
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(PaneLineHeight),
        )
    }
    ShimmerBox(
        modifier = Modifier
            .fillMaxWidth(PANE_LAST_LINE_FRACTION)
            .height(PaneLineHeight),
    )
}

private const val PASSAGE_ROW_COUNT = 3
private const val PANE_LINE_COUNT = 4
private const val PANE_LAST_LINE_FRACTION = 0.5f
private const val READING_WEIGHT = 2f
private const val STUDY_WEIGHT = 3f
private val ReadingContentMaxWidth = 560.dp
private val StudyContentMaxWidth = 560.dp
private val PortraitContentMaxWidth = 600.dp
private val StudyCardHeight = 88.dp
private val ButtonHeight = 44.dp
private val PassageLabelWidth = 120.dp
private val PassageLabelHeight = 18.dp
private val CheckboxSize = 24.dp
private val NotesLabelWidth = 80.dp
private val NotesLabelHeight = 20.dp
private val NotesFieldHeight = 120.dp
private val PaneTitleWidth = 160.dp
private val PaneTitleHeight = 24.dp
private val PaneSubtitleWidth = 100.dp
private val PaneSubtitleHeight = 16.dp
private val PaneTabsHeight = 36.dp
private val PaneLineHeight = 14.dp
