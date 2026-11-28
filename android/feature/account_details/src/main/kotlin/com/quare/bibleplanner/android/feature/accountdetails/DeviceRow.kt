package com.quare.bibleplanner.android.feature.accountdetails

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material.icons.outlined.TabletMac
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.android.designsystem.date.relativeTimeLabel
import com.quare.bibleplanner.core.devices.domain.model.DeviceFormFactor
import com.quare.bibleplanner.feature.accountdetails.presentation.model.DeviceUiModel

private const val SIGNING_OUT_ALPHA = 0.5f
private val spinnerSize = 18.dp
private val spinnerBoxSize = 48.dp

@Composable
internal fun DeviceRow(
    device: DeviceUiModel,
    onRenameClick: () -> Unit,
    onSignOutClick: () -> Unit,
) {
    val contentAlpha = if (device.isSigningOut) SIGNING_OUT_ALPHA else 1f
    ListItem(
        headlineContent = {
            DeviceNameRow(
                device = device,
                modifier = Modifier.alpha(contentAlpha),
            )
        },
        supportingContent = {
            Text(
                text = deviceDetailLine(device),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.alpha(contentAlpha),
            )
        },
        leadingContent = {
            Icon(
                imageVector = device.formFactor.toIcon(),
                contentDescription = null,
                modifier = Modifier.alpha(contentAlpha),
            )
        },
        trailingContent = {
            if (device.isSigningOut) {
                DeviceSignOutSpinner()
            } else {
                DeviceMenu(
                    onRenameClick = onRenameClick,
                    onSignOutClick = onSignOutClick,
                )
            }
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}

@Composable
private fun DeviceNameRow(
    device: DeviceUiModel,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = device.name,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(
                weight = 1f,
                fill = false,
            ),
        )
        if (device.isCurrentDevice) {
            Spacer(modifier = Modifier.width(8.dp))
            CurrentDeviceBadge()
        }
    }
}

@Composable
private fun DeviceMenu(
    onRenameClick: () -> Unit,
    onSignOutClick: () -> Unit,
) {
    var isMenuExpanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { isMenuExpanded = true }) {
            Icon(
                imageVector = Icons.Rounded.MoreVert,
                contentDescription = stringResource(R.string.account_details_device_options),
            )
        }
        DropdownMenu(
            expanded = isMenuExpanded,
            onDismissRequest = { isMenuExpanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(text = stringResource(R.string.account_details_rename)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = null,
                    )
                },
                onClick = {
                    isMenuExpanded = false
                    onRenameClick()
                },
            )
            DropdownMenuItem(
                text = { Text(text = stringResource(R.string.account_details_sign_out)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Logout,
                        contentDescription = null,
                    )
                },
                onClick = {
                    isMenuExpanded = false
                    onSignOutClick()
                },
                colors = MenuDefaults.itemColors(
                    textColor = MaterialTheme.colorScheme.error,
                    leadingIconColor = MaterialTheme.colorScheme.error,
                ),
            )
        }
    }
}

@Composable
private fun DeviceSignOutSpinner() {
    Box(
        modifier = Modifier.size(spinnerBoxSize),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(spinnerSize),
            strokeWidth = 2.dp,
        )
    }
}

@Composable
private fun CurrentDeviceBadge() {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ) {
        Text(
            text = stringResource(R.string.account_details_this_device),
            modifier = Modifier.padding(
                horizontal = 7.dp,
                vertical = 2.dp,
            ),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun deviceDetailLine(device: DeviceUiModel): String {
    val relative = relativeTimeLabel(device.lastActive)
    return device.locationLine?.let { location -> "$location · $relative" } ?: relative
}

private fun DeviceFormFactor.toIcon(): ImageVector = when (this) {
    DeviceFormFactor.PHONE -> Icons.Outlined.Smartphone
    DeviceFormFactor.TABLET -> Icons.Outlined.TabletMac
    DeviceFormFactor.COMPUTER -> Icons.Outlined.Computer
    DeviceFormFactor.UNKNOWN -> Icons.Outlined.Devices
}
