package com.quare.bibleplanner.ui.component.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun DeleteIconButon(
    modifier: Modifier = Modifier,
    contentDescription: String,
    onClick: () -> Unit,
) {
    CommonIconButton(
        modifier = modifier,
        imageVector = Icons.Default.Delete,
        contentDescription = contentDescription,
        onClick = onClick,
    )
}
