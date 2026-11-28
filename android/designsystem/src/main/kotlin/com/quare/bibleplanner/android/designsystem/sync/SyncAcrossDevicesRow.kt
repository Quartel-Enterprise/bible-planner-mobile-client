package com.quare.bibleplanner.android.designsystem.sync

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.android.designsystem.R

private const val LOGGED_OUT_ALPHA = 0.55f

/*
 * Why: logged out, the row stays tappable instead of disabled, so the tap can explain that syncing needs
 * an account.
 */
@Composable
fun SyncAcrossDevicesRow(
    description: String,
    isChecked: Boolean,
    isLoggedIn: Boolean,
    onToggle: (Boolean) -> Unit,
    onBlockedClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isLoggedIn) {
                    Modifier.toggleable(
                        value = isChecked,
                        role = Role.Switch,
                        onValueChange = onToggle,
                    )
                } else {
                    Modifier.clickable(
                        role = Role.Switch,
                        onClick = onBlockedClick,
                    )
                },
            ).padding(vertical = 12.dp)
            .alpha(if (isLoggedIn) 1f else LOGGED_OUT_ALPHA),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.sync_across_devices_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = isChecked && isLoggedIn,
            onCheckedChange = null,
            enabled = isLoggedIn,
        )
    }
}
