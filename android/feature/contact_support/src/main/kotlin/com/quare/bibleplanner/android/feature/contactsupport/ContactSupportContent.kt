package com.quare.bibleplanner.android.feature.contactsupport

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.android.designsystem.icon.SheetIconTile
import com.quare.bibleplanner.android.designsystem.language.toNameRes
import com.quare.bibleplanner.core.provider.billing.domain.model.SubscriptionStatus
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.feature.contactsupport.domain.model.AccountStatusModel
import com.quare.bibleplanner.feature.contactsupport.domain.model.SupportContact
import com.quare.bibleplanner.feature.contactsupport.presentation.model.ContactSupportUiEvent
import com.quare.bibleplanner.feature.contactsupport.presentation.model.ContactSupportUiState
import com.quare.bibleplanner.ui.utils.sheet.SheetExitAnimationEffect
import com.quare.bibleplanner.ui.utils.sheet.blockPointerInput
import com.quare.bibleplanner.ui.utils.sheet.rememberSheetCloseGuard
import software.quare.loadable.valueOrNull

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ContactSupportContent(
    uiState: ContactSupportUiState,
    snackbarHostState: SnackbarHostState,
    onEvent: (ContactSupportUiEvent) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val sheetCloseGuard = rememberSheetCloseGuard()
    // Why: a double tap on open email would otherwise launch the email app twice.
    SheetExitAnimationEffect(
        animateOut = sheetState::hide,
        animateBackIn = sheetState::show,
    )
    ModalBottomSheet(
        onDismissRequest = { sheetCloseGuard.close { onEvent(ContactSupportUiEvent.OnDismiss) } },
        sheetState = sheetState,
        sheetGesturesEnabled = !sheetCloseGuard.isClosing,
    ) {
        Box {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .blockPointerInput(isBlocked = sheetCloseGuard.isClosing)
                    .fillMaxWidth()
                    .padding(
                        start = 24.dp,
                        end = 24.dp,
                        bottom = 16.dp,
                    ).verticalScroll(rememberScrollState()),
            ) {
                SheetIconTile(imageVector = Icons.Rounded.Email)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.contact_support_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.contact_support_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(20.dp))
                InfoCard {
                    EmailToRow(onCopyClick = { onEvent(ContactSupportUiEvent.OnCopyEmailClick) })
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.contact_support_diagnostics_header).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                DiagnosticsCard(uiState = uiState)
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        onEvent(ContactSupportUiEvent.OnSendEmailClick)
                    },
                ) {
                    Icon(
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                        imageVector = Icons.Rounded.Email,
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                    Text(text = stringResource(R.string.contact_support_open_email))
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onEvent(ContactSupportUiEvent.OnCopyEmailClick) },
                ) {
                    Text(text = stringResource(R.string.contact_support_copy_email))
                }
            }
            SnackbarHost(
                modifier = Modifier.align(Alignment.BottomCenter),
                hostState = snackbarHostState,
            )
        }
    }
}

@Composable
private fun DiagnosticsCard(uiState: ContactSupportUiState) {
    InfoCard {
        DiagnosticsRow(
            label = stringResource(R.string.contact_support_app_version_label),
            value = uiState.appVersion,
        )
        DiagnosticsRow(
            label = stringResource(R.string.contact_support_platform_label),
            value = stringResource(uiState.platform.toLabel()),
        )
        DiagnosticsRow(
            label = stringResource(R.string.contact_support_language_label),
            value = uiState.selectedLanguage
                .valueOrNull()
                ?.let { language -> stringResource(language.toNameRes()) }
                .orEmpty(),
        )
        DiagnosticsRow(
            label = stringResource(R.string.contact_support_account_label),
            value = uiState.accountStatusModel.toDisplayText(),
        )
        DiagnosticsRow(
            label = stringResource(R.string.contact_support_subscription_label),
            value = uiState.subscriptionStatus
                .valueOrNull()
                ?.let { status -> stringResource(status.toLabel()) }
                .orEmpty(),
        )
    }
}

@Composable
private fun InfoCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = MaterialTheme.shapes.large,
            ).padding(
                horizontal = 16.dp,
                vertical = 4.dp,
            ),
        content = content,
    )
}

@Composable
private fun EmailToRow(onCopyClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = Icons.Rounded.AlternateEmail,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.contact_support_email_to_label),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = SupportContact.EMAIL,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
        }
        IconButton(onClick = onCopyClick) {
            Icon(
                imageVector = Icons.Rounded.ContentCopy,
                contentDescription = stringResource(R.string.contact_support_copy_email),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DiagnosticsRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        // Why: a long value such as the account email must wrap itself, not squeeze the label to a sliver.
        Text(
            modifier = Modifier.weight(1f),
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun AccountStatusModel.toDisplayText(): String = when (this) {
    is AccountStatusModel.LoggedIn -> user.email

    AccountStatusModel.LoggedOut, AccountStatusModel.Loading, AccountStatusModel.Error ->
        stringResource(R.string.contact_support_account_not_connected)
}

@StringRes
private fun Platform.toLabel(): Int = when (this) {
    Platform.Android -> R.string.contact_support_platform_android
    Platform.Ios -> R.string.contact_support_platform_ios
    is Platform.Desktop -> R.string.contact_support_platform_desktop
    Platform.Web -> R.string.contact_support_platform_web
}

@StringRes
private fun SubscriptionStatus.toLabel(): Int = when (this) {
    SubscriptionStatus.Free -> R.string.contact_support_subscription_free
    is SubscriptionStatus.Pro -> R.string.contact_support_subscription_pro
}
