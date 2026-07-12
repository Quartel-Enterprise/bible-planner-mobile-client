package com.quare.bibleplanner.feature.accountdetails.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import bibleplanner.feature.account_details.generated.resources.Res
import bibleplanner.feature.account_details.generated.resources.account_details_title
import com.quare.bibleplanner.core.model.loadable.valueOrNull
import com.quare.bibleplanner.feature.accountdetails.presentation.content.AccountInfoSection
import com.quare.bibleplanner.feature.accountdetails.presentation.content.ConnectedDevicesSection
import com.quare.bibleplanner.feature.accountdetails.presentation.content.LogoutRow
import com.quare.bibleplanner.feature.accountdetails.presentation.model.AccountDetailsUiEvent
import com.quare.bibleplanner.feature.accountdetails.presentation.model.AccountDetailsUiState
import com.quare.bibleplanner.feature.accountdetails.presentation.utils.AccountDetailsUiActionCollector
import com.quare.bibleplanner.feature.accountdetails.presentation.viewmodel.AccountDetailsViewModel
import com.quare.bibleplanner.ui.component.ResponsiveDialogSheet
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun AccountDetailsSheet(
    onNavigateBack: () -> Unit,
    onNavigateReplacingTop: (NavKey) -> Unit,
    onNavigate: (NavKey) -> Unit,
) {
    val viewModel = koinViewModel<AccountDetailsViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    AccountDetailsUiActionCollector(
        uiActionFlow = viewModel.uiAction,
        onNavigateReplacingTop = onNavigateReplacingTop,
        onNavigate = onNavigate,
    )
    ResponsiveDialogSheet(
        onCloseClick = onNavigateBack,
        title = stringResource(Res.string.account_details_title),
    ) {
        AccountDetailsContent(uiState = uiState, onEvent = viewModel::onEvent)
    }
}

@Composable
private fun AccountDetailsContent(
    uiState: AccountDetailsUiState,
    onEvent: (AccountDetailsUiEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 8.dp)
            .padding(bottom = 16.dp),
    ) {
        AccountInfoSection(accountInfo = uiState.accountInfo)
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        ConnectedDevicesSection(
            devices = uiState.devices,
            isExpanded = uiState.isDevicesExpanded,
            onEvent = onEvent,
        )
        if (uiState.accountInfo.valueOrNull() != null) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            LogoutRow(onClick = { onEvent(AccountDetailsUiEvent.OnLogoutClick) })
        }
    }
}
