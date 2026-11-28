package com.quare.bibleplanner.android.feature.books

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

private val anchorSpacing = 8.dp

@Composable
internal fun BooksDropdownMenu(
    isExpanded: Boolean,
    onDismissRequest: () -> Unit,
    options: List<BooksMenuOption>,
) {
    DropdownMenu(
        expanded = isExpanded,
        onDismissRequest = onDismissRequest,
        offset = DpOffset(
            x = 0.dp,
            y = anchorSpacing,
        ),
    ) {
        options.forEach { option ->
            DropdownMenuItem(
                text = { Text(text = option.title) },
                onClick = option.onClick,
                trailingIcon = if (option.isSelected) {
                    {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = stringResource(R.string.books_selected),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                } else {
                    null
                },
            )
        }
    }
}
