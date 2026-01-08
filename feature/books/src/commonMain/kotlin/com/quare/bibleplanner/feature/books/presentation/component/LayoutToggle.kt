package com.quare.bibleplanner.feature.books.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import bibleplanner.feature.books.generated.resources.Res
import bibleplanner.feature.books.generated.resources.grid
import bibleplanner.feature.books.generated.resources.list
import com.quare.bibleplanner.feature.books.presentation.model.BookLayoutFormat
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiEvent
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun LayoutToggle(
    selectedLayoutFormat: BookLayoutFormat,
    onEvent: (BooksUiEvent) -> Unit,
) {
    Row(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = CircleShape,
            ).padding(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        val formats = listOf(BookLayoutFormat.List, BookLayoutFormat.Grid)
        formats.forEach { format ->
            val isSelected = selectedLayoutFormat == format
            val shape = CircleShape

            Box(
                modifier = Modifier
                    .height(32.dp)
                    .weight(1f)
                    .clip(shape)
                    .background(
                        if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            Color.Transparent
                        },
                    ).clickable { onEvent(BooksUiEvent.OnLayoutFormatSelect(format)) },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = when (format) {
                        BookLayoutFormat.List -> Icons.Default.List
                        BookLayoutFormat.Grid -> Icons.Default.GridView
                    },
                    contentDescription = stringResource(
                        when (format) {
                            BookLayoutFormat.List -> Res.string.list
                            BookLayoutFormat.Grid -> Res.string.grid
                        },
                    ),
                    tint = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}
