package com.quare.bibleplanner.feature.more.presentation.content.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import bibleplanner.feature.more.generated.resources.Res
import bibleplanner.feature.more.generated.resources.edit_profile_action
import org.jetbrains.compose.resources.stringResource

private val expandedBreakpoint = 600.dp
private val editIconSize = 22.dp

@Composable
internal fun EditProfileButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = stringResource(Res.string.edit_profile_action)
    val windowWidth = LocalWindowInfo.current.containerSize.width
    val isExpanded = with(LocalDensity.current) { windowWidth.toDp() } >= expandedBreakpoint
    if (isExpanded) {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(editIconSize),
                )
                Text(text = label)
            }
        }
    } else {
        IconButton(
            onClick = onClick,
            modifier = modifier,
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = label,
                modifier = Modifier.size(editIconSize),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
