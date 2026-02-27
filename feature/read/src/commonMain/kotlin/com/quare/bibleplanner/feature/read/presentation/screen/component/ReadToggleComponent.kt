package com.quare.bibleplanner.feature.read.presentation.screen.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
internal fun ReadToggleComponent(
    modifier: Modifier = Modifier,
    isChecked: Boolean,
    toggleReadStatus: () -> Unit,
) {
    Row(
        modifier = modifier.clickable { toggleReadStatus() },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("Read")
        Checkbox(
            checked = isChecked,
            onCheckedChange = { toggleReadStatus() }
        )
    }
}
