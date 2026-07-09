package com.quare.bibleplanner.feature.contactsupport.presentation.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import bibleplanner.feature.contact_support.generated.resources.Res
import bibleplanner.feature.contact_support.generated.resources.contact_support_diagnostics_header
import bibleplanner.feature.contact_support.generated.resources.contact_support_dialog_description
import bibleplanner.feature.contact_support.generated.resources.contact_support_dialog_title
import bibleplanner.feature.contact_support.generated.resources.contact_support_email_to_label
import bibleplanner.feature.contact_support.generated.resources.copy_email_address_button
import bibleplanner.feature.contact_support.generated.resources.diagnostics_account_label
import bibleplanner.feature.contact_support.generated.resources.diagnostics_account_not_connected
import bibleplanner.feature.contact_support.generated.resources.diagnostics_app_version_label
import bibleplanner.feature.contact_support.generated.resources.diagnostics_language_label
import bibleplanner.feature.contact_support.generated.resources.diagnostics_platform_label
import bibleplanner.feature.contact_support.generated.resources.diagnostics_subscription_label
import bibleplanner.feature.contact_support.generated.resources.open_email_button
import com.quare.bibleplanner.core.model.loadable.valueOrNull
import com.quare.bibleplanner.feature.contactsupport.domain.model.AccountStatusModel
import com.quare.bibleplanner.feature.contactsupport.domain.model.SupportContact
import com.quare.bibleplanner.feature.contactsupport.presentation.model.ContactSupportUiEvent
import com.quare.bibleplanner.feature.contactsupport.presentation.model.ContactSupportUiState
import com.quare.bibleplanner.feature.contactsupport.presentation.model.toStringResource
import com.quare.bibleplanner.ui.component.icon.CommonIconButton
import com.quare.bibleplanner.ui.component.spacer.HorizontalSpacer
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ContactSupportDialogContent(
    state: ContactSupportUiState,
    onEvent: (ContactSupportUiEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(color = MaterialTheme.colorScheme.primaryContainer, shape = CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }

        VerticalSpacer(16.dp)

        Text(
            text = stringResource(Res.string.contact_support_dialog_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        VerticalSpacer(8.dp)

        Text(
            text = stringResource(Res.string.contact_support_dialog_description),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        VerticalSpacer(20.dp)

        DiagnosticsCard {
            EmailToRow(onCopyClick = { onEvent(ContactSupportUiEvent.OnCopyEmailClick) })
        }

        VerticalSpacer(16.dp)

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(Res.string.contact_support_diagnostics_header).uppercase(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        VerticalSpacer(8.dp)

        DiagnosticsCard {
            DiagnosticsRow(
                label = stringResource(Res.string.diagnostics_app_version_label),
                value = state.appVersion,
            )
            DiagnosticsRow(
                label = stringResource(Res.string.diagnostics_platform_label),
                value = stringResource(state.platform.toStringResource()),
            )
            DiagnosticsRow(
                label = stringResource(Res.string.diagnostics_language_label),
                value = state.selectedLanguage
                    .valueOrNull()
                    ?.let { stringResource(it.toStringResource()) }
                    .orEmpty(),
            )
            DiagnosticsRow(
                label = stringResource(Res.string.diagnostics_account_label),
                value = state.accountStatusModel.toDisplayText(),
            )
            DiagnosticsRow(
                label = stringResource(Res.string.diagnostics_subscription_label),
                value = state.subscriptionStatus
                    .valueOrNull()
                    ?.let { stringResource(it.toStringResource()) }
                    .orEmpty(),
            )
        }

        VerticalSpacer(24.dp)

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                shape = PillShape,
                onClick = { onEvent(ContactSupportUiEvent.OnSendEmailClick) },
            ) {
                Icon(imageVector = Icons.Default.Email, contentDescription = null)
                HorizontalSpacer(8.dp)
                Text(text = stringResource(Res.string.open_email_button))
            }
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                shape = PillShape,
                onClick = { onEvent(ContactSupportUiEvent.OnCopyEmailClick) },
            ) {
                Text(text = stringResource(Res.string.copy_email_address_button))
            }
        }
    }
}

@Composable
private fun DiagnosticsCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(16.dp),
            ).padding(horizontal = 16.dp, vertical = 4.dp),
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
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Default.AlternateEmail,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Column {
                Text(
                    text = stringResource(Res.string.contact_support_email_to_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = SupportContact.EMAIL,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
        CommonIconButton(
            imageVector = Icons.Default.ContentCopy,
            contentDescription = stringResource(Res.string.copy_email_address_button),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            onClick = onCopyClick,
        )
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
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun AccountStatusModel.toDisplayText(): String = when (this) {
    is AccountStatusModel.LoggedIn -> {
        user.email
    }

    AccountStatusModel.LoggedOut, AccountStatusModel.Loading, AccountStatusModel.Error -> {
        stringResource(Res.string.diagnostics_account_not_connected)
    }
}

private val PillShape = RoundedCornerShape(percent = 50)
