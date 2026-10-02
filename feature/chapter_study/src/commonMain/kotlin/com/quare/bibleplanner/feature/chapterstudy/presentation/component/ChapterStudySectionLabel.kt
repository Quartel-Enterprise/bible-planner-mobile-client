package com.quare.bibleplanner.feature.chapterstudy.presentation.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val topSpacing = 18.dp
private val bottomSpacing = 8.dp
private val letterSpacing = 0.5.sp

@Composable
internal fun ChapterStudySectionLabel(
    text: String,
    modifier: Modifier = Modifier,
    isFirst: Boolean = false,
) {
    Text(
        text = text,
        modifier = modifier
            .padding(
                top = if (isFirst) 0.dp else topSpacing,
                bottom = bottomSpacing,
            ).semantics { heading() },
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        letterSpacing = letterSpacing,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
