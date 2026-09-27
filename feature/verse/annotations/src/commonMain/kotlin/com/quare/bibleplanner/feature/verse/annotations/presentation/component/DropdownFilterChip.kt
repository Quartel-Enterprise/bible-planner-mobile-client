package com.quare.bibleplanner.feature.verse.annotations.presentation.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.ui.component.AppDropdownMenu
import com.quare.bibleplanner.ui.component.AppDropdownMenuItem

@Composable
internal fun DropdownFilterChip(
    label: String,
    icon: ImageVector,
    isActive: Boolean,
    isExpanded: Boolean,
    menuItems: List<AppDropdownMenuItem>,
    onClick: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        FilterChip(
            selected = isActive,
            onClick = onClick,
            label = { Text(text = label) },
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
            },
        )
        AppDropdownMenu(
            isExpanded = isExpanded,
            onDismissRequest = onDismissRequest,
            items = menuItems,
        )
    }
}
