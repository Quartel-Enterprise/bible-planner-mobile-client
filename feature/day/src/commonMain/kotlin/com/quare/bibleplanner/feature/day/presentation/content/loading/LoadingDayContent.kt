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

private val readingContentMaxWidth = 560.dp
private val studyContentMaxWidth = 560.dp
private val portraitContentMaxWidth = 600.dp
private val studyCardHeight = 88.dp
private val buttonHeight = 44.dp
private val passageLabelWidth = 120.dp
private val passageLabelHeight = 18.dp
private val checkboxSize = 24.dp
private val notesLabelWidth = 80.dp
private val notesLabelHeight = 20.dp
private val notesFieldHeight = 120.dp
private val paneTitleWidth = 160.dp
private val paneTitleHeight = 24.dp
private val paneSubtitleWidth = 100.dp
private val paneSubtitleHeight = 16.dp
private val paneTabsHeight = 36.dp
private val paneLineHeight = 14.dp
private const val PASSAGE_ROW_COUNT = 3
private const val PANE_LINE_COUNT = 4
private const val PANE_LAST_LINE_FRACTION = 0.5f
private const val READING_WEIGHT = 2f
private const val STUDY_WEIGHT = 3f

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
                .widthIn(max = portraitContentMaxWidth)
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
                    .widthIn(max = readingContentMaxWidth)
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
            maxWidth = studyContentMaxWidth,
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
            .height(studyCardHeight),
        shape = RoundedCornerShape(18.dp),
    )
}

@Composable
private fun ButtonSkeleton() {
    ShimmerBox(
        modifier = Modifier
            .fillMaxWidth()
            .height(buttonHeight),
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
                            .width(passageLabelWidth)
                            .height(passageLabelHeight),
                    )
                    ShimmerBox(
                        modifier = Modifier.size(checkboxSize),
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
                .width(notesLabelWidth)
                .height(notesLabelHeight),
        )
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(notesFieldHeight),
            shape = RoundedCornerShape(12.dp),
        )
    }
}

@Composable
private fun StudyPaneSkeleton() {
    ShimmerBox(
        modifier = Modifier
            .width(paneTitleWidth)
            .height(paneTitleHeight),
    )
    ShimmerBox(
        modifier = Modifier
            .width(paneSubtitleWidth)
            .height(paneSubtitleHeight),
    )
    ShimmerBox(
        modifier = Modifier
            .fillMaxWidth()
            .height(paneTabsHeight),
        shape = RoundedCornerShape(8.dp),
    )
    repeat(PANE_LINE_COUNT) {
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(paneLineHeight),
        )
    }
    ShimmerBox(
        modifier = Modifier
            .fillMaxWidth(PANE_LAST_LINE_FRACTION)
            .height(paneLineHeight),
    )
}
