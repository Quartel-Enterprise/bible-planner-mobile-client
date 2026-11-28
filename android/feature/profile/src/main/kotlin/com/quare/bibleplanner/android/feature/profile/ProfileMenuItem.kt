package com.quare.bibleplanner.android.feature.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp

private const val ICON_ALPHA = 0.5f

@Composable
internal fun ProfileMenuItem(
    title: String,
    icon: Painter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    isSubtitleLoading: Boolean = false,
    iconColor: Color = LocalContentColor.current.copy(alpha = ICON_ALPHA),
    headlineColor: Color = Color.Unspecified,
    headlineModifier: Modifier = Modifier,
    trailingContent: @Composable (() -> Unit)? = null,
) {
    ListItem(
        headlineContent = {
            Text(
                text = title,
                color = headlineColor,
                modifier = headlineModifier,
            )
        },
        supportingContent = when {
            isSubtitleLoading -> {
                { ProfileSkeletonBox(modifier = Modifier.width(120.dp).height(14.dp)) }
            }

            subtitle != null -> {
                { Text(text = subtitle) }
            }

            else -> null
        },
        leadingContent = {
            Icon(
                painter = icon,
                contentDescription = title,
                tint = iconColor,
            )
        },
        modifier = modifier.clickable(onClick = onClick),
        trailingContent = trailingContent,
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}

@Composable
internal fun ProfileMenuSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ProfileSectionHeader(title = title)
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                content = content,
            )
        }
    }
}

@Composable
internal fun ProfileSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier,
        text = title,
        style = MaterialTheme.typography.labelMedium,
    )
}
