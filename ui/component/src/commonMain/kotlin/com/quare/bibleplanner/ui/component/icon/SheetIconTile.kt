package com.quare.bibleplanner.ui.component.icon

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

private val tileSize = 56.dp
private val tileCornerRadius = 16.dp
private val iconSize = 28.dp

@Composable
fun SheetIconTile(
    imageVector: ImageVector,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(tileSize)
            .clip(RoundedCornerShape(tileCornerRadius))
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            modifier = Modifier.size(iconSize),
            imageVector = imageVector,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}
