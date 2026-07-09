package com.quare.bibleplanner.feature.more.presentation.content.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
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
import bibleplanner.feature.more.generated.resources.Res
import bibleplanner.feature.more.generated.resources.contact_support_dialog_description
import bibleplanner.feature.more.generated.resources.contact_support_dialog_title
import bibleplanner.feature.more.generated.resources.copy_support_email_button
import bibleplanner.feature.more.generated.resources.send_support_email_button
import com.quare.bibleplanner.feature.more.domain.model.SupportContact
import com.quare.bibleplanner.feature.more.presentation.model.MoreUiEvent
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ContactSupportDialogContent(onEvent: (MoreUiEvent) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
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

        VerticalSpacer(8.dp)

        Text(
            text = SupportContact.EMAIL,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
        )

        VerticalSpacer(24.dp)

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onEvent(MoreUiEvent.OnSendSupportEmailClick) },
            ) {
                Text(text = stringResource(Res.string.send_support_email_button))
            }
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onEvent(MoreUiEvent.OnCopySupportEmailClick) },
            ) {
                Text(text = stringResource(Res.string.copy_support_email_button))
            }
        }
    }
}
