package com.quare.bibleplanner.ui.component.sync

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import bibleplanner.ui.component.generated.resources.Res
import bibleplanner.ui.component.generated.resources.sync_across_devices_title
import com.quare.bibleplanner.ui.component.AppSwitch
import org.jetbrains.compose.resources.stringResource

private const val LOGGED_OUT_ALPHA = 0.55f

@Composable
fun SyncAcrossDevicesOption(
    description: String,
    isChecked: Boolean,
    isLoggedIn: Boolean,
    onToggle: (Boolean) -> Unit,
    onBlockedClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.alpha(if (isLoggedIn) 1f else LOGGED_OUT_ALPHA),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(Res.string.sync_across_devices_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        SyncSwitch(
            isChecked = isChecked,
            isLoggedIn = isLoggedIn,
            onToggle = onToggle,
            onBlockedClick = onBlockedClick,
        )
    }
}

// Why: a disabled switch swallows taps, so a transparent layer on top catches them to explain the login.
@Composable
private fun SyncSwitch(
    isChecked: Boolean,
    isLoggedIn: Boolean,
    onToggle: (Boolean) -> Unit,
    onBlockedClick: () -> Unit,
) {
    Box {
        AppSwitch(
            checked = isChecked && isLoggedIn,
            onCheckedChange = onToggle.takeIf { isLoggedIn },
            enabled = isLoggedIn,
        )
        if (!isLoggedIn) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onBlockedClick,
                    ),
            )
        }
    }
}
