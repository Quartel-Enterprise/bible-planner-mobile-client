package com.quare.bibleplanner.feature.applanguage.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private const val SELECTED_CONTAINER_ALPHA = 0.12f

@Composable
internal fun AppLanguageItem(
    code: String,
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        headlineContent = { Text(name, fontWeight = if (isSelected) FontWeight.SemiBold else null) },
        leadingContent = { LanguageCodeChip(code = code, isSelected = isSelected) },
        trailingContent = {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primary.copy(alpha = SELECTED_CONTAINER_ALPHA)
            } else {
                Color.Transparent
            },
        ),
        modifier = modifier.clickable(onClick = onClick),
    )
}

@Composable
private fun LanguageCodeChip(
    code: String,
    isSelected: Boolean,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(
                if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.secondaryContainer
                },
            ),
    ) {
        Text(
            text = code,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (isSelected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSecondaryContainer
            },
        )
    }
}
