package com.quare.bibleplanner.feature.applanguage.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.material3.ListItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight

@Composable
internal fun AppLanguageItem(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        headlineContent = { Text(name, fontWeight = if (isSelected) FontWeight.SemiBold else null) },
        trailingContent = {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
            )
        },
        modifier = modifier.clickable(onClick = onClick),
    )
}
