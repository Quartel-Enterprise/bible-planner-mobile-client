package com.quare.bibleplanner.feature.day.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.ui.component.shimmer.ShimmerBox

@Composable
internal fun DayHeaderTitleSkeleton(
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        ShimmerBox(
            modifier = Modifier
                .width(TitleWidth)
                .height(TitleHeight),
        )
        ShimmerBox(
            modifier = Modifier
                .width(SubtitleWidth)
                .height(SubtitleHeight),
        )
    }
}

private val TitleWidth = 140.dp
private val TitleHeight = 22.dp
private val SubtitleWidth = 90.dp
private val SubtitleHeight = 14.dp
