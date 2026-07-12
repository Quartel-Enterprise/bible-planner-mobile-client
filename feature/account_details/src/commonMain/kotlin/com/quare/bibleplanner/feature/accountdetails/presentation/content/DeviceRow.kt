package com.quare.bibleplanner.feature.accountdetails.presentation.content

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.TabletMac
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import bibleplanner.feature.account_details.generated.resources.Res
import bibleplanner.feature.account_details.generated.resources.account_details_device_options
import bibleplanner.feature.account_details.generated.resources.account_details_rename
import bibleplanner.feature.account_details.generated.resources.account_details_sign_out
import bibleplanner.feature.account_details.generated.resources.account_details_this_device
import com.quare.bibleplanner.core.devices.domain.model.DeviceFormFactor
import com.quare.bibleplanner.feature.accountdetails.presentation.model.AccountDetailsUiEvent
import com.quare.bibleplanner.feature.accountdetails.presentation.model.DeviceUiModel
import com.quare.bibleplanner.ui.component.spacer.HorizontalSpacer
import com.quare.bibleplanner.ui.utils.format
import org.jetbrains.compose.resources.stringResource

private const val SIGNING_OUT_ALPHA = 0.5f

@Composable
internal fun DeviceRow(
    device: DeviceUiModel,
    onEvent: (AccountDetailsUiEvent) -> Unit,
) {
    val contentAlpha = if (device.isSigningOut) SIGNING_OUT_ALPHA else 1f
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = device.formFactor.icon(),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.alpha(contentAlpha),
        )
        HorizontalSpacer(14.dp)
        Column(modifier = Modifier.weight(1f).alpha(contentAlpha)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = device.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(weight = 1f, fill = false),
                )
                if (device.isCurrentDevice) {
                    HorizontalSpacer(8.dp)
                    CurrentDeviceBadge()
                }
            }
            Text(
                text = device.detailLine(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (device.isSigningOut) {
            DeviceSignOutSpinner()
        } else {
            DeviceMenu(device = device, onEvent = onEvent)
        }
    }
}

@Composable
private fun DeviceMenu(
    device: DeviceUiModel,
    onEvent: (AccountDetailsUiEvent) -> Unit,
) {
    var isMenuExpanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { isMenuExpanded = true }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = stringResource(Res.string.account_details_device_options),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        DropdownMenu(
            expanded = isMenuExpanded,
            onDismissRequest = { isMenuExpanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.account_details_rename)) },
                onClick = {
                    isMenuExpanded = false
                    onEvent(AccountDetailsUiEvent.OnRenameDeviceClick(device))
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null)
                },
            )
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(Res.string.account_details_sign_out),
                        color = MaterialTheme.colorScheme.error,
                    )
                },
                onClick = {
                    isMenuExpanded = false
                    onEvent(AccountDetailsUiEvent.OnSignOutDeviceClick(device))
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                    )
                },
            )
        }
    }
}

@Composable
private fun DeviceSignOutSpinner() {
    Box(
        modifier = Modifier.size(48.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(18.dp),
            strokeWidth = 2.dp,
        )
    }
}

@Composable
private fun CurrentDeviceBadge() {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Text(
            text = stringResource(Res.string.account_details_this_device),
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Composable
private fun DeviceUiModel.detailLine(): String {
    val relative = lastActive.format()
    return locationLine?.let { location -> "$location · $relative" } ?: relative
}

private fun DeviceFormFactor.icon(): ImageVector = when (this) {
    DeviceFormFactor.PHONE -> Icons.Default.Smartphone
    DeviceFormFactor.TABLET -> Icons.Default.TabletMac
    DeviceFormFactor.COMPUTER -> Icons.Default.Computer
    DeviceFormFactor.UNKNOWN -> Icons.Default.Devices
}
