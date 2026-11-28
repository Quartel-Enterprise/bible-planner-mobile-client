package com.quare.bibleplanner.android.feature.books

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.core.books.presentation.model.titleRes
import com.quare.bibleplanner.feature.books.presentation.model.BookLayoutFormat
import com.quare.bibleplanner.feature.books.presentation.model.BookTestamentFilter
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiEvent
import org.jetbrains.compose.resources.stringResource as sharedStringResource

private val layoutOptionSize = 48.dp
private val layoutIconSize = 18.dp

@Composable
internal fun BooksToggleRow(
    selectedTestamentFilter: BookTestamentFilter,
    isTestamentMenuVisible: Boolean,
    selectedLayoutFormat: BookLayoutFormat,
    onEvent: (BooksUiEvent) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.weight(1f)) {
            TestamentFilterChip(
                selectedTestamentFilter = selectedTestamentFilter,
                isMenuVisible = isTestamentMenuVisible,
                onEvent = onEvent,
            )
        }
        LayoutFormatToggle(
            selectedLayoutFormat = selectedLayoutFormat,
            onEvent = onEvent,
        )
    }
}

@Composable
private fun TestamentFilterChip(
    selectedTestamentFilter: BookTestamentFilter,
    isMenuVisible: Boolean,
    onEvent: (BooksUiEvent) -> Unit,
) {
    Box {
        Surface(
            modifier = Modifier.semantics { role = Role.DropdownList },
            onClick = { onEvent(BooksUiEvent.OnToggleTestamentMenu) },
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
            ),
        ) {
            Row(
                modifier = Modifier.padding(
                    horizontal = 14.dp,
                    vertical = 8.dp,
                ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    modifier = Modifier.weight(
                        weight = 1f,
                        fill = false,
                    ),
                    text = testamentFilterLabel(selectedTestamentFilter),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
                Icon(
                    modifier = Modifier.size(18.dp),
                    imageVector = if (isMenuVisible) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        BooksDropdownMenu(
            isExpanded = isMenuVisible,
            onDismissRequest = { onEvent(BooksUiEvent.OnDismissTestamentMenu) },
            options = BookTestamentFilter.entries.map { testamentFilter ->
                BooksMenuOption(
                    title = testamentFilterLabel(testamentFilter),
                    isSelected = testamentFilter == selectedTestamentFilter,
                    onClick = { onEvent(BooksUiEvent.OnTestamentSelect(testamentFilter)) },
                )
            },
        )
    }
}

@Composable
private fun testamentFilterLabel(testamentFilter: BookTestamentFilter): String {
    val testament = testamentFilter.testament ?: return stringResource(R.string.books_all_testaments)
    return sharedStringResource(testament.titleRes)
}

@Composable
private fun LayoutFormatToggle(
    selectedLayoutFormat: BookLayoutFormat,
    onEvent: (BooksUiEvent) -> Unit,
) {
    Row(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = CircleShape,
            ).padding(2.dp)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        BookLayoutFormat.entries.forEach { format ->
            LayoutFormatOption(
                format = format,
                isSelected = selectedLayoutFormat == format,
                onClick = { onEvent(BooksUiEvent.OnLayoutFormatSelect(format)) },
            )
        }
    }
}

@Composable
private fun LayoutFormatOption(
    format: BookLayoutFormat,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(layoutOptionSize)
            .clip(CircleShape)
            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
            .selectable(
                selected = isSelected,
                role = Role.RadioButton,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = format.toIcon(),
            contentDescription = stringResource(format.toLabelRes()),
            tint = if (isSelected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.size(layoutIconSize),
        )
    }
}

private fun BookLayoutFormat.toIcon(): ImageVector = when (this) {
    BookLayoutFormat.List -> Icons.AutoMirrored.Filled.List
    BookLayoutFormat.Grid -> Icons.Default.GridView
}

private fun BookLayoutFormat.toLabelRes(): Int = when (this) {
    BookLayoutFormat.List -> R.string.books_layout_list
    BookLayoutFormat.Grid -> R.string.books_layout_grid
}
