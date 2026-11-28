package com.quare.bibleplanner.android.feature.accountdetails

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.accountdetails.presentation.model.DeviceUiModel
import software.quare.loadable.Loadable

private const val SKELETON_ROWS = 2

@Composable
internal fun ConnectedDevicesSection(
    devices: Loadable<List<DeviceUiModel>>,
    isExpanded: Boolean,
    onToggleClick: () -> Unit,
    onRenameClick: (DeviceUiModel) -> Unit,
    onSignOutClick: (DeviceUiModel) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        DevicesHeader(
            devices = devices,
            isExpanded = isExpanded,
            onClick = onToggleClick,
        )
        AnimatedVisibility(visible = isExpanded) {
            when (devices) {
                Loadable.Loading -> DevicesSkeleton()

                is Loadable.Loaded -> Column {
                    devices.value.forEach { device ->
                        DeviceRow(
                            device = device,
                            onRenameClick = { onRenameClick(device) },
                            onSignOutClick = { onSignOutClick(device) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DevicesHeader(
    devices: Loadable<List<DeviceUiModel>>,
    isExpanded: Boolean,
    onClick: () -> Unit,
) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = { Text(text = stringResource(R.string.account_details_connected_devices)) },
        supportingContent = { DevicesCountLabel(devices = devices) },
        leadingContent = {
            Icon(
                imageVector = Icons.Outlined.Devices,
                contentDescription = null,
            )
        },
        trailingContent = {
            Icon(
                imageVector = if (isExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                contentDescription = null,
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}

@Composable
private fun DevicesCountLabel(devices: Loadable<List<DeviceUiModel>>) {
    when (devices) {
        Loadable.Loading -> PlaceholderBox(
            modifier = Modifier
                .padding(top = 4.dp)
                .fillMaxWidth(0.3f)
                .height(12.dp),
        )

        is Loadable.Loaded -> Text(
            text = pluralStringResource(
                R.plurals.account_details_devices_active,
                devices.value.size,
                devices.value.size,
            ),
        )
    }
}

@Composable
private fun DevicesSkeleton() {
    Column {
        repeat(SKELETON_ROWS) {
            ListItem(
                headlineContent = {
                    PlaceholderBox(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(14.dp),
                    )
                },
                supportingContent = {
                    PlaceholderBox(
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .fillMaxWidth(0.7f)
                            .height(12.dp),
                    )
                },
                leadingContent = { PlaceholderBox(modifier = Modifier.size(24.dp)) },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            )
        }
    }
}
