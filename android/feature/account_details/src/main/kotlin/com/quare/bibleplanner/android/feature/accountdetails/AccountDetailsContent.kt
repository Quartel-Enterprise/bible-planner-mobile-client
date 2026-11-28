package com.quare.bibleplanner.android.feature.accountdetails

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.accountdetails.presentation.model.AccountDetailsUiEvent
import com.quare.bibleplanner.feature.accountdetails.presentation.model.AccountDetailsUiState
import com.quare.bibleplanner.feature.accountdetails.presentation.model.DeviceUiModel
import com.quare.bibleplanner.ui.utils.sheet.SheetExitAnimationEffect
import com.quare.bibleplanner.ui.utils.sheet.blockPointerInput
import com.quare.bibleplanner.ui.utils.sheet.rememberSheetCloseGuard
import software.quare.loadable.valueOrNull

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AccountDetailsContent(
    uiState: AccountDetailsUiState,
    onEvent: (AccountDetailsUiEvent) -> Unit,
    onClose: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val sheetCloseGuard = rememberSheetCloseGuard()
    SheetExitAnimationEffect(
        animateOut = sheetState::hide,
        animateBackIn = sheetState::show,
    )
    ModalBottomSheet(
        onDismissRequest = { sheetCloseGuard.close(onClose) },
        sheetState = sheetState,
        sheetGesturesEnabled = !sheetCloseGuard.isClosing,
    ) {
        Column(
            modifier = Modifier
                .blockPointerInput(isBlocked = sheetCloseGuard.isClosing)
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = stringResource(R.string.account_details_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(
                    start = 24.dp,
                    end = 24.dp,
                    bottom = 8.dp,
                ),
            )
            AccountInfoSection(accountInfo = uiState.accountInfo)
            SectionDividerLine()
            ConnectedDevicesSection(
                devices = uiState.devices,
                isExpanded = uiState.isDevicesExpanded,
                onToggleClick = { onEvent(AccountDetailsUiEvent.OnToggleDevices) },
                /*
                 * Why: the rename dialog opens over this sheet without popping it, and the guard's pending close
                 * expires on its own, so it keeps a double tap from opening the dialog twice.
                 */
                onRenameClick = { device ->
                    sheetCloseGuard.close { onEvent(AccountDetailsUiEvent.OnRenameDeviceClick(device)) }
                },
                onSignOutClick = { device -> handleSignOutClick(device, sheetCloseGuard::close, onEvent) },
            )
            if (uiState.accountInfo.valueOrNull() != null) {
                SectionDividerLine()
                LogoutRow(onClick = { sheetCloseGuard.close { onEvent(AccountDetailsUiEvent.OnLogoutClick) } })
            }
        }
    }
}

private fun handleSignOutClick(
    device: DeviceUiModel,
    close: (() -> Unit) -> Unit,
    onEvent: (AccountDetailsUiEvent) -> Unit,
) {
    val event = AccountDetailsUiEvent.OnSignOutDeviceClick(device)
    // Why: signing out this device replaces the sheet with the logout dialog, while another device stays in the list.
    if (device.isCurrentDevice) {
        close { onEvent(event) }
    } else {
        onEvent(event)
    }
}

@Composable
private fun SectionDividerLine() {
    HorizontalDivider(
        modifier = Modifier.padding(
            horizontal = 16.dp,
            vertical = 4.dp,
        ),
    )
}

@Composable
private fun LogoutRow(onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = { Text(text = stringResource(R.string.account_details_logout)) },
        leadingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.Logout,
                contentDescription = null,
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent,
            headlineColor = MaterialTheme.colorScheme.error,
            leadingIconColor = MaterialTheme.colorScheme.error,
        ),
    )
}
