package com.quare.bibleplanner.feature.accountdetails.presentation.content

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bibleplanner.feature.account_details.generated.resources.Res
import bibleplanner.feature.account_details.generated.resources.account_details_connected_devices
import bibleplanner.feature.account_details.generated.resources.account_details_devices_active
import com.quare.bibleplanner.core.model.loadable.Loadable
import com.quare.bibleplanner.feature.accountdetails.presentation.model.AccountDetailsUiEvent
import com.quare.bibleplanner.feature.accountdetails.presentation.model.DeviceUiModel
import com.quare.bibleplanner.ui.component.shimmer.ShimmerBox
import com.quare.bibleplanner.ui.component.spacer.HorizontalSpacer
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

private const val SHIMMER_ROWS = 2

@Composable
internal fun ConnectedDevicesSection(
    devices: Loadable<List<DeviceUiModel>>,
    isExpanded: Boolean,
    onEvent: (AccountDetailsUiEvent) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        DevicesHeader(
            devices = devices,
            isExpanded = isExpanded,
            onClick = { onEvent(AccountDetailsUiEvent.OnToggleDevices) },
        )
        AnimatedVisibility(visible = isExpanded) {
            when (devices) {
                Loadable.Loading -> DevicesShimmerList()

                is Loadable.Loaded -> Column {
                    devices.value.forEach { device ->
                        DeviceRow(device = device, onEvent = onEvent)
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.Devices,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        HorizontalSpacer(14.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(Res.string.account_details_connected_devices),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            DevicesCountLabel(devices)
        }
        Icon(
            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DevicesCountLabel(devices: Loadable<List<DeviceUiModel>>) {
    when (devices) {
        Loadable.Loading -> ShimmerBox(
            modifier = Modifier
                .padding(top = 4.dp)
                .fillMaxWidth(0.3f)
                .height(12.dp),
        )

        is Loadable.Loaded -> Text(
            text = pluralStringResource(
                Res.plurals.account_details_devices_active,
                devices.value.size,
                devices.value.size,
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DevicesShimmerList() {
    Column {
        repeat(SHIMMER_ROWS) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ShimmerBox(modifier = Modifier.size(24.dp))
                HorizontalSpacer(14.dp)
                Column(modifier = Modifier.weight(1f)) {
                    ShimmerBox(modifier = Modifier.fillMaxWidth(0.5f).height(14.dp))
                    VerticalSpacer(6.dp)
                    ShimmerBox(modifier = Modifier.fillMaxWidth(0.7f).height(12.dp))
                }
            }
        }
    }
}
