package com.quare.bibleplanner.ui.component.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.ui.theme.success

private val iconSize = 18.dp
private val iconSpacing = 10.dp

@Composable
fun DialogConsequenceRow(
    type: DialogConsequenceType,
    text: String,
    modifier: Modifier = Modifier,
) {
    val (icon, tint) = when (type) {
        DialogConsequenceType.REMOVED -> Icons.Default.Close to MaterialTheme.colorScheme.error
        DialogConsequenceType.KEPT -> Icons.Default.Check to MaterialTheme.success
    }
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(iconSpacing),
    ) {
        Icon(
            modifier = Modifier.size(iconSize),
            imageVector = icon,
            contentDescription = null,
            tint = tint,
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
