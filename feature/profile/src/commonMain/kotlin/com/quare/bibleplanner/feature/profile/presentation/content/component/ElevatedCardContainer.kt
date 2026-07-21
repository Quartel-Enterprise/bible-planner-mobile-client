package com.quare.bibleplanner.feature.profile.presentation.content.component

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun ElevatedCardContainer(
    isClickable: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (isClickable) {
        ElevatedCard(
            onClick = onClick,
            modifier = modifier,
            shape = MaterialTheme.shapes.large,
            content = content,
        )
    } else {
        ElevatedCard(
            modifier = modifier,
            shape = MaterialTheme.shapes.large,
            content = content,
        )
    }
}
