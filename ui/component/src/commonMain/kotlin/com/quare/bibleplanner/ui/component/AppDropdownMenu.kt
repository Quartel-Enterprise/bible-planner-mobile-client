package com.quare.bibleplanner.ui.component

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

private val anchorSpacing = 8.dp

@Composable
fun AppDropdownMenu(
    isExpanded: Boolean,
    onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    DropdownMenu(
        expanded = isExpanded,
        onDismissRequest = onDismissRequest,
        offset = DpOffset(
            x = 0.dp,
            y = anchorSpacing,
        ),
        content = content,
    )
}
