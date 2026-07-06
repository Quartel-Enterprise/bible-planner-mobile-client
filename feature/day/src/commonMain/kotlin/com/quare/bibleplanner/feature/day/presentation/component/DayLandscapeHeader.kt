package com.quare.bibleplanner.feature.day.presentation.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.ui.component.icon.BackIcon

@Composable
internal fun DayLandscapeHeader(
    platform: Platform,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BackIcon(
            platform = platform,
            onBackClick = onBackClick,
        )
        title()
    }
}
