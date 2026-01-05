package com.quare.bibleplanner.feature.more.presentation.content.component

import androidx.compose.foundation.clickable
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.quare.bibleplanner.feature.more.presentation.model.MoreMenuItemPresentationModel
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MoreMenuItem(
    itemModel: MoreMenuItemPresentationModel,
    subtitle: String? = null,
    onClick: () -> Unit,
    isDestructive: Boolean = false,
) {
    val text = stringResource(itemModel.name)
    ListItem(
        headlineContent = {
            Text(text = text)
        },
        supportingContent = subtitle?.let { { Text(it) } },
        leadingContent = {
            MoreItemIcon(
                icon = itemModel.icon,
                contentDescription = text,
                tint = if (isDestructive) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                },
            )
        },
        modifier = Modifier.clickable(onClick = onClick),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}
